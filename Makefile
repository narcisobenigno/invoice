postgres_credential_default := {"host":"localhost:5432","dbname":"invoice","username":"user","password":"password"}

export POSTGRES_CREDENTIAL := $(or ${POSTGRES_CREDENTIAL},${postgres_credential_default})

.PHONY: clean
clean:
	@./gradlew clean

.PHONY: test
test:
	@./gradlew test

.PHONY: integration
integration:
	@./gradlew integration

.PHONY: test.all
test.all: clean
	@./gradlew check