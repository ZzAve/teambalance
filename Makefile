.PHONY: *
MAKEFLAGS += -j2

# The first command will be invoked with `make` only and should be `build`
build:
	./mvnw install -Pformat

ci:
	./mvnw -B clean install org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=ZzAve_teambalance
	./mvnw -B \
		clean install \
		org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
		-Dsonar.projectKey=ZzAve_teambalance

test: run-local-backend
	./mvnw spring-boot:run -pl test-data -Dspring-boot.run.arguments="--host=http://localhost:8080 --apiKey=dGVhbWJhbGFuY2U= --strict"


deploy:
	./mvnw -B clean verify jib:build -DskipTests -Dnpm.lint.skip

format:
	./mvnw test-compile -Pformat

clean:
	./mvnw clean

update:
	./mvnw versions:update-parent versions:update-properties versions:use-latest-versions

yolo:
	./mvnw install -T0.5C -DskipTests -Dnpm.ci.skip -Dnpm.install.skip=false -Dnpm.lint.skip

db:
	docker compose up --wait postgresql

run-local: run-local-backend run-local-frontend

run-local-backend:
	docker compose up --wait backend

run-local-frontend:
	./mvnw frontend:npm@start -pl frontend

e2e-prepare:
	./mvnw frontend:npx@playwright-install -pl e2e

e2e: e2e-prepare
	./mvnw frontend:npm@start -pl e2e

e2e-report:
	./e2e/node_modules/.bin/playwright show-report e2e/playwright-report
