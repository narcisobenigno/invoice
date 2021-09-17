postgres_credential_default := {"host":"localhost:5432","dbname":"invoice","username":"user","password":"password"}

export POSTGRES_CREDENTIAL := $(or ${POSTGRES_CREDENTIAL},${postgres_credential_default})

.PHONY: test
test:
	@./gradlew test