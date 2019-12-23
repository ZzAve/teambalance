package nl.jvandis.teambalance.api.bank;

import com.bunq.sdk.context.ApiContext;
import com.bunq.sdk.context.ApiEnvironmentType;
import com.bunq.sdk.context.BunqContext;
import com.bunq.sdk.exception.BunqException;
import com.bunq.sdk.exception.ForbiddenException;
import com.bunq.sdk.http.Pagination;
import com.bunq.sdk.model.generated.endpoint.*;
import com.bunq.sdk.model.generated.object.Amount;
import com.bunq.sdk.model.generated.object.Pointer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BunqRepository {

    private static final int DEFAULT_FETCH_COUNT = 10;

    /**
     * Error constants.
     */
    private static final String ERROR_COULD_NOT_FIND_ALIAS_TYPE_IBAN = "Could not find alias with type IBAN for monetary account \"%s\"";
    private static final String ERROR_COULD_NOT_GENERATE_NEW_API_KEY = "Encountered error while retrieving new sandbox ApiKey.\nError message %s";
    private static final String ERROR_COULD_NOT_FIND_CONFIG_FILE = "Could not find a bunq configuration to load.";

    /**
     * FileName constatns.
     */
    private static final String FILE_NAME_BUNQ_CONF_PRODUCTION = "bunq-production.conf";
    private static final String FILE_NAME_BUNQ_CONF_SANDBOX = "bunq-sandbox.conf";

    /**
     * Field constatns.
     */
    private static final String FIELD_RESPONSE = "Response";
    private static final String FIELD_API_KEY = "ApiKey";

    /**
     *
     */
    private static final String POINTER_TYPE_EMAIL = "EMAIL";
    private static final String CURRENCY_EUR = "EUR";
    private static final String DEVICE_SERVER_DESCRIPTION = "bunq Tinker java";

    /**
     * The index of the fist item in an array.
     */
    private static final int INDEX_FIRST = 0;

    /**
     * Http constants.
     */
    private static final int HTTP_STATUS_OK = 200;
    private static final String ERROR_COULD_NOT_DETERMINE_USER_TYPE = "Could not determine user type";

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

    private ApiEnvironmentType environmentType;

    private String apiKey;

    private User user;

    public BunqRepository(ApiEnvironmentType environmentType,
                          String apiKey) throws UnknownHostException {
        this.environmentType = environmentType;
        this.apiKey = apiKey;

        this.setupContext();
        this.setupCurrentUser();
        this.requestSpendingMoneyIfNeeded();
    }


    private void createApiConfig() throws UnknownHostException {
        ApiContext.create(
                ApiEnvironmentType.PRODUCTION,
                apiKey,
                InetAddress.getLocalHost().getHostName()
        ).save("bunq-production.conf");
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
        if (new File(this.determineBunqConfigFileName()).exists()) {
            // Config is already present.
        } else if (ApiEnvironmentType.SANDBOX.equals(this.environmentType)) {
            SandboxUser sandboxUser = generateNewSandboxUser();
            ApiContext.create(ApiEnvironmentType.SANDBOX, sandboxUser.getApiKey(), DEVICE_SERVER_DESCRIPTION).save(this.determineBunqConfigFileName());
        } else {
            createApiConfig();
        }

        try {
            ApiContext apiContext = ApiContext.restore(this.determineBunqConfigFileName());

            apiContext.ensureSessionActive();
            apiContext.save(this.determineBunqConfigFileName());

            BunqContext.loadApiContext(apiContext);
        } catch (ForbiddenException forbiddenException) {
            if (resetConfigIfNeeded) {
                this.handleForbiddenException(forbiddenException);
            } else {
                throw forbiddenException;
            }
        }
    }

    public void updateContext() {
        BunqContext.getApiContext().save(this.determineBunqConfigFileName());
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

    /**
     *
     */
    private void setupCurrentUser() {
        this.user = User.get().getValue();
    }

    public MonetaryAccountBank getMonetaryAccountBank(int id) {
        return MonetaryAccountBank.get(id).getValue();
    }

    public List<Payment> getAllPayment(int accountId, int count) {
        Pagination pagination = new Pagination();
        pagination.setCount(count);

        return Payment.list(
                accountId,
                pagination.getUrlParamsCountOnly()
        ).getValue();
    }


    private SandboxUser generateNewSandboxUser() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://public-api.sandbox.bunq.com/v1/sandbox-user")
                .post(RequestBody.create(null, new byte[0]))
                .addHeader("x-bunq-client-request-id", "1234")
                .addHeader("cache-control", "no-cache")
                .addHeader("x-bunq-geolocation", "0 0 0 0 NL")
                .addHeader("x-bunq-language", "en_US")
                .addHeader("x-bunq-region", "en_US")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.code() == HTTP_STATUS_OK) {
                String responseString = response.body().string();
                JsonObject jsonObject = new Gson().fromJson(responseString, JsonObject.class);
                JsonObject apiKEy = jsonObject.getAsJsonArray(FIELD_RESPONSE).get(INDEX_FIRST).getAsJsonObject().get(FIELD_API_KEY).getAsJsonObject();

                return SandboxUser.fromJsonReader(new JsonReader(new StringReader(apiKEy.toString())));
            } else {
                throw new BunqException(String.format(ERROR_COULD_NOT_GENERATE_NEW_API_KEY, response.body().string()));
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
                System.out.println(exception.getMessage());
            }
        }
    }

    private boolean shouldRequestSpendingMoney() {
        return ApiEnvironmentType.SANDBOX.equals(environmentType)
                && (Double.parseDouble(BunqContext.getUserContext().getPrimaryMonetaryAccountBank().getBalance().getValue())
                <= BALANCE_ZERO);
    }
}
