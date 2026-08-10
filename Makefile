.PHONY: verify run package self-audit

verify:
	mvn clean verify

run:
	mvn spring-boot:run

package:
	mvn clean package

self-audit:
	./scripts/self-audit.sh
