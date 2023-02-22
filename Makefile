#!make

M := "mvn"


.PHONY: build
build: clean fmt
	${M} spotbugs:spotbugs
	${M} install

.PHONY: fmt
fmt:
	${M} spotless:apply

.PHONY: clean
clean:
	${M} clean
