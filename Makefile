#!make

M := "mvn"

.PHONY: build
build: clean fmt
	${M} install
	${M} spotbugs:spotbugs

.PHONY: compile
compile:
	${M} compile

.PHONY: package
package:
	${M} package

.PHONY: fmt
fmt:
	${M} spotless:apply

.PHONY: clean
clean:
	${M} clean
