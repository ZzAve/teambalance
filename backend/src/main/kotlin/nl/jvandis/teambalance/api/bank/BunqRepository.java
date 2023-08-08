package nl.jvandis.teambalance.api.bank;

import com.bunq.sdk.context.ApiContext;
import com.bunq.sdk.context.ApiEnvironmentType;
import com.bunq.sdk.context.BunqContext;
import com.bunq.sdk.exception.BunqException;
import com.bunq.sdk.exception.ForbiddenException;
import com.bunq.sdk.http.BunqHeader;
import com.bunq.sdk.http.Pagination;
import com.bunq.sdk.model.generated.endpoint.MonetaryAccountBank;
import com.bunq.sdk.model.generated.endpoint.Payment;
import com.bunq.sdk.model.generated.endpoint.RequestInquiry;
import com.bunq.sdk.model.generated.endpoint.SandboxUserPerson;
import com.bunq.sdk.model.generated.object.Amount;
import com.bunq.sdk.model.generated.object.Pointer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import jakarta.annotation.Nullable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * This repository translates teambalance's domain into bunq domain, and performs requests to Bunq through its Java SDK.
 * <p>
 * Copy/pasted and adapted from <a href="https://github.com/bunq/tinker_java">Bunq's tinker project </a>
 */
public class BunqRepository {
    private static final Logger log = LoggerFactory.getLogger(BunqRepository.class);

    /**
     * Error constants.
     */
    private static final String ERROR_COULD_NOT_GENERATE_NEW_API_KEY = "Encountered error while retrieving new sandbox ApiKey.\nError message %s";

    /**
     * FileName constants.
     */
    private static final String FILE_NAME_BUNQ_CONF_PRODUCTION = "bunq-production.conf";
    private static final String FILE_NAME_BUNQ_CONF_SANDBOX = "bunq-sandbox.conf";

    /**
     * Field constants.
     */
    private static final String FIELD_RESPONSE = "Response";
    private static final String FIELD_API_KEY = "ApiKey";

    /**
     *
     */
    private static final String POINTER_TYPE_EMAIL = "EMAIL";
    private static final String CURRENCY_EUR = "EUR";
    private static final String DEVICE_SERVER_DESCRIPTION = "Teambalance SANDBOX app";

    /**
     * The index of the fist item in an array.
     */
    private static final int INDEX_FIRST = 0;

    /**
     * Http constants.
     */
    private static final int HTTP_STATUS_OK = 200;

    /**
     * Request spending money constants.
     */
    private static final String REQUEST_SPENDING_MONEY_AMOUNT = "500.0";
    private static final String REQUEST_SPENDING_MONEY_RECIPIENT = "sugardaddy@bunq.com";
    private static final String REQUEST_SPENDING_MONEY_DESCRIPTION = "Requesting some spending money.";
    private static final int REQUEST_SPENDING_MONEY_WAIT_TIME_MILLISECONDS = 1000;

    /**
     * Balance constant.
     */
    private static final double BALANCE_ZERO = 0.0;

    /**
     * Concurrency constants
     */

    private static final Duration acquireMaxWaitDuration = Duration.ofSeconds(5);
    /**
     * A semaphore is used to limit the amount of open calls to Bunq to 1.
     */
    private static final Semaphore semaphore = new Semaphore(1);
    private final Map<Integer, Integer> accountIdsToValidatedAccountIds = new HashMap<>();
    private final ApiEnvironmentType environmentType;
    private final String apiKey;
    private final Boolean saveSessionToFile;

    public BunqRepository(ApiEnvironmentType environmentType,
                          @Nullable String apiKey,
                          Boolean saveSessionToFile) throws UnknownHostException {
        assert (ApiEnvironmentType.PRODUCTION != environmentType) || (apiKey != null);
        log.info("Starting BunqRepository of type {}", environmentType);

        this.environmentType = environmentType;
        this.apiKey = apiKey;
        this.saveSessionToFile = saveSessionToFile;

        this.setupContext();
        this.requestSpendingMoneyIfNeeded();
    }

    public BunqRepository(BankBunqConfig config) throws UnknownHostException {
        this(
                toApiEnvironmentType(config.getEnvironment()),
                config.getApiKey(),
                config.getSaveSessionToFile()
        );
    }

    private Integer checkAccountId(Integer accountId) {
        return accountIdsToValidatedAccountIds.computeIfAbsent(accountId, x -> {
            if (environmentType == ApiEnvironmentType.PRODUCTION) {
                return this.validateAccountId(accountId);
            } else {
                return this.getAccountId();
            }
        });
    }

    private int getAccountId() {
        Pagination pagination = new Pagination();
        pagination.setCount(100);

        List<MonetaryAccountBank> allAccount = MonetaryAccountBank.list(pagination.getUrlParamsCountOnly()).getValue();

        return allAccount.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There are no active accounts enabled for the current user / session")).getId();
    }

    private int validateAccountId(Integer accountId) {
        Pagination pagination = new Pagination();
        pagination.setCount(100);

        List<MonetaryAccountBank> allAccount = MonetaryAccountBank.list(pagination.getUrlParamsCountOnly()).getValue();

        return allAccount.stream()
                .filter(monetaryAccountBank -> accountId.equals(monetaryAccountBank.getId()))
                .findFirst().orElseThrow(() -> {
                    final String error = String.format("There is no account with id %s present " +
                            "and/or enabled for access with the current API key ", accountId);
                    log.error(error);
                    return new IllegalStateException(error);
                }).getId();
    }

    private static ApiEnvironmentType toApiEnvironmentType(BunqEnvironment environment) {
        return switch (environment) {
            case PRODUCTION -> ApiEnvironmentType.PRODUCTION;
            case SANDBOX -> ApiEnvironmentType.SANDBOX;
        };
    }

    public MonetaryAccountBank getMonetaryAccountBank(int accountId) {
        return withSemaphore(() -> {
            var validatedAccountId = checkAccountId(accountId);
            return MonetaryAccountBank.get(validatedAccountId).getValue();
        });
    }

    public List<Payment> getAllPayments(int accountId, int count) {
        return withSemaphore(() -> {
            Pagination pagination = new Pagination();
            pagination.setCount(count);
            var validatedAccountId = checkAccountId(accountId);
            return Payment.list(
                    validatedAccountId,
                    pagination.getUrlParamsCountOnly()
            ).getValue();

        });
    }

    public void updateContext() {
        safeSave(BunqContext.getApiContext());
    }

    private <T> T withSemaphore(Supplier<T> action) {
        boolean permit = false;
        // TODO: use try with resources (autoclosable ?)
        try {
            permit = semaphore.tryAcquire(acquireMaxWaitDuration.getSeconds(), TimeUnit.SECONDS);
            if (permit) {
                return action.get();
            } else {
                log.warn("Could not acquire semaphore");
                throw new RuntimeException("Could not acquire semaphore");
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            if (permit) {
                semaphore.release();
            }
        }
    }

    private ApiContext createApiConfig() throws UnknownHostException {
        ArrayList<String> permittedIps = new ArrayList<>();
        permittedIps.add("*");
        return ApiContext.create(
                this.environmentType,
                apiKey,
                InetAddress.getLocalHost().getHostName(),
                permittedIps
        );
    }

    /**
     *
     */
    private void setupContext() throws UnknownHostException {
        this.setupContext(true);
    }

    /**
     *
     */
    private void setupContext(boolean resetConfigIfNeeded) throws UnknownHostException {
        ApiContext apiContext;
        if (new File(this.determineBunqConfigFileName()).exists()) {
            // Config is already present.
            apiContext = ApiContext.restore(this.determineBunqConfigFileName());
        } else if (ApiEnvironmentType.SANDBOX.equals(this.environmentType)) {
            SandboxUserPerson sandboxUser = generateNewSandboxUser();
            apiContext = ApiContext.create(ApiEnvironmentType.SANDBOX, sandboxUser.getApiKey(), DEVICE_SERVER_DESCRIPTION);

        } else {
            log.info("No API config found. Creating new API config.");
            apiContext = createApiConfig();
            log.info("Created new API config.");
        }

        try {
            apiContext.ensureSessionActive();
            safeSave(apiContext);

            BunqContext.loadApiContext(apiContext);
        } catch (ForbiddenException forbiddenException) {
            if (resetConfigIfNeeded) {
                this.handleForbiddenException(forbiddenException);
            } else {
                throw forbiddenException;
            }
        }
    }

    private void safeSave(ApiContext apiContext) {
        if (saveSessionToFile) {
            apiContext.save(this.determineBunqConfigFileName());
        } else {
            log.info("Skipping saving context to file");
        }
    }

    /**
     * @return String
     */
    private String determineBunqConfigFileName() {
        if (ApiEnvironmentType.PRODUCTION.equals(this.environmentType)) {
            return FILE_NAME_BUNQ_CONF_PRODUCTION;
        } else {
            return FILE_NAME_BUNQ_CONF_SANDBOX;
        }
    }

    /**
     *
     */
    private void handleForbiddenException(ForbiddenException forbiddenException) throws UnknownHostException {
        if (ApiEnvironmentType.SANDBOX.equals(this.environmentType)) {
            this.deleteOldConfig();
            this.setupContext(false);
        } else {
            throw forbiddenException;
        }
    }

    /**
     *
     */
    private void deleteOldConfig() {
        try {
            Files.delete(Paths.get((this.determineBunqConfigFileName())));
        } catch (IOException e) {
            throw new BunqException(e.getMessage());
        }
    }


    private SandboxUserPerson generateNewSandboxUser() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(
                        "https://" +
                                ApiEnvironmentType.SANDBOX.getBaseUri() +
                                "/" +
                                ApiEnvironmentType.SANDBOX.getApiVersion() +
                                "/sandbox-user-person"
                )
                .post(RequestBody.create(null, new byte[INDEX_FIRST]))
                .addHeader(BunqHeader.CLIENT_REQUEST_ID.getHeaderName(), UUID.randomUUID().toString())
                .addHeader(
                        BunqHeader.CACHE_CONTROL.getHeaderName(), BunqHeader.CACHE_CONTROL.getDefaultValue()
                )
                .addHeader(BunqHeader.GEOLOCATION.getHeaderName(), BunqHeader.GEOLOCATION.getDefaultValue())
                .addHeader(BunqHeader.LANGUAGE.getHeaderName(), BunqHeader.LANGUAGE.getDefaultValue())
                .addHeader(BunqHeader.REGION.getHeaderName(), BunqHeader.REGION.getDefaultValue())
                .build();

        try {
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == HTTP_STATUS_OK) {
                    String responseString = response.body().string();
                    JsonObject jsonObject = new Gson().fromJson(responseString, JsonObject.class);
                    JsonObject apiKEy = jsonObject.getAsJsonArray(FIELD_RESPONSE).get(INDEX_FIRST).getAsJsonObject().get(FIELD_API_KEY).getAsJsonObject();

                    return SandboxUserPerson.fromJsonReader(new JsonReader(new StringReader(apiKEy.toString())));
                } else {
                    throw new BunqException(String.format(ERROR_COULD_NOT_GENERATE_NEW_API_KEY, response.body().string()));
                }
            }
        } catch (IOException e) {
            throw new BunqException(e.getMessage());
        }
    }

    private void requestSpendingMoneyIfNeeded() {
        if (shouldRequestSpendingMoney()) {
            RequestInquiry.create(
                    new Amount(REQUEST_SPENDING_MONEY_AMOUNT, CURRENCY_EUR),
                    new Pointer(POINTER_TYPE_EMAIL, REQUEST_SPENDING_MONEY_RECIPIENT),
                    REQUEST_SPENDING_MONEY_DESCRIPTION,
                    false
            );

            try {
                Thread.sleep(REQUEST_SPENDING_MONEY_WAIT_TIME_MILLISECONDS);
            } catch (InterruptedException exception) {
                log.warn(exception.getMessage());
            }
        }
    }

    private boolean shouldRequestSpendingMoney() {
        return ApiEnvironmentType.SANDBOX.equals(environmentType)
                && (Double.parseDouble(BunqContext.getUserContext().getPrimaryMonetaryAccountBank().getBalance().getValue())
                <= BALANCE_ZERO);
    }
}
