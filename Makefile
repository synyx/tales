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

.PHONY: all go js \
	build build-go build-js \
	clean clean-go clean-js \
	coverage coverage-go coverage-js \
	lint lint-go lint-js \
	test test-go test-js \
	dist

all: build

go: build-go

js: build-js

build: build-go build-js

build-go: ${BINARIES}

bin/%: cmd/**/*.go pkg/**/*.go
	go build $(GOFLAGS) -o $@ $(PKG)/cmd/$(subst $(SUFFIX),,$(@:bin/%=%))

build-js:
	npm run build

coverage: coverage-go coverage-js

coverage-go: coverage.html

coverage.html: coverage.out
	go tool cover -html=coverage.out -o coverage.html

coverage.out: pkg/**/*_test.go
	go test -coverprofile=coverage.out ./pkg/...

coverage-js: coverage/index.html

coverage/index.html:
	npx jest --coverage --coverageReporters html

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
	if which zip; then \
		cd dist && zip -r tales-server.zip tales-server; \
	else \
		cd dist && 7z a tales-server.zip tales-server; \
	fi

clean: clean-go clean-js

clean-go:
	rm -rf coverage.out coverage.html ${BINARIES}

clean-js:
	rm -rf dist/main.{js,js.map} public/js/tales.{js,js.map}
