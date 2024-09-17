.PHONY: *

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

