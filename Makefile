.PHONY: *
MAKEFLAGS += -j2

# The first command will be invoked with `make` only and should be `build`
build:
	./mvnw install -Pformat

ci:
	./mvnw -B \
		clean install \
		org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
		-Dsonar.projectKey=ZzAve_teambalance

test: run-local-backend
	./mvnw spring-boot:run -pl test-data -Dspring-boot.run.arguments="--host=http://localhost:8080 --apiKey=dGVhbWJhbGFuY2U= --username=admin --password=admin"

e2e:
	(rm -rf ./e2e/playwright-report/index.html || true) && \
		mkdir -p ./e2e/playwright-report && \
		(echo "E2E tests are running ... " > ./e2e/playwright-report/index.html) && \
		docker compose up --wait --build && docker compose logs e2e -f

e2e-report:
	cd e2e && npm run report

deploy:
	./mvnw -B clean verify jib:build -DskipTests -Dnpm.lint.skip

format:
	./mvnw test-compile -Pformat

clean:
	./mvnw clean && docker compose down

update:
	./mvnw versions:update-parent versions:update-properties versions:use-latest-versions

yolo:
	./mvnw install -T0.5C -DskipTests -Dverification.skip -Dnpm.ci.skip -Dnpm.install.skip=false -Dnpm.lint.skip

db:
	docker compose up --wait postgresql

run-local: run-local-backend run-local-frontend

run-local-backend:
	docker compose up --wait backend

rerun-local-backend:
	docker compose restart backend

run-local-frontend:
	docker compose up --wait frontend
	#cd frontend && npm start

