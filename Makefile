.PHONY: *
MAKEFLAGS += -j2

# The first command will be invoked with `make` only and should be `build`
build:
	./mvnw install -Pformat

ci:
	./mvnw clean install

format:
	./mvnw test-compile -Pformat

update:
	./mvnw versions:update-parent versions:update-properties versions:use-latest-versions

yolo:
	./mvnw install -T0.5C -DskipTests -Dnpm.ci.skip -Dnpm.install.skip=false -Dnpm.lint.skip

run-local: run-local-backend run-local-frontend

run-local-backend: yolo
	./mvnw spring-boot:run -Dspring-boot.run.profiles=local -pl app

run-local-frontend: yolo
	./mvnw frontend:npm@start -pl frontend

e2e:
	./mvnw frontend:npm@start -pl e2e
