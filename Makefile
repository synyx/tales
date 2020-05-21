PKG := synyx.de/tales
CMDS := tales-server tales-migrate

VERSION ?= $(shell git describe --tags --abbrev=0 2> /dev/null)
GIT_SHA := $(shell git rev-parse HEAD)
GIT_DIRTY := $(shell git status --porcelain | grep -v "??" 2> /dev/null)
GIT_TREE_STATE := $(if $(GIT_DIRTY),"dirty","clean")

LDFLAGS := -X $(PKG)/pkg/buildinfo.Version=$(VERSION) \
	-X $(PKG)/pkg/buildinfo.GitSHA=$(GIT_SHA) \
	-X $(PKG)/pkg/buildinfo.GitTreeState=$(GIT_TREE_STATE)
GOFLAGS := -ldflags "$(LDFLAGS)"

ifeq ($(GOOS), windows)
SUFFIX := .exe
endif

BINDIR=bin/
PKGDIR=pkg/
BINARIES=$(addsuffix ${SUFFIX},$(addprefix ${BINDIR},${CMDS}))

.PHONY: all build clean coverage coverage-go coverage-js lint lint-go lint-js test test-go test-js dist

all: build

build: ${BINARIES} public/js/tales.js

bin/%: pkg/**/*.go
	go build $(GOFLAGS) -o $@ $(PKG)/cmd/$(subst $(SUFFIX),,$(@:bin/%=%))

coverage: coverage-go coverage-js

coverage-go: coverage.html

coverage.html: coverage.out
	go tool cover -html=coverage.out -o coverage.html

coverage.out: pkg/**/*_test.go
	go test -coverprofile=coverage.out ./pkg/...

coverage-js: coverage/index.html

coverage/index.html:
	npx jest --coverage --coverageReporters html

public/js/tales.js: js/**/*.js
	npm run build

lint: lint-go lint-js

lint-go:
	go vet ./...
	golint -set_exit_status ./...

lint-js:
	npm run lint

test: test-go test-js

test-go:
	go test ./pkg/...

test-js:
	npm run test

dist: tales-server.zip

tales-server.zip: bin/* public/*
	mkdir -p dist/tales-server
	cp -r bin public dist/tales-server/
	cd dist && zip -r tales-server.zip tales-server

clean:
	rm -f coverage.out coverage.html dist \
		public/js/tales.{js,js.map} \
		${BINARIES}