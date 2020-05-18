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
TARGETS=$(addsuffix ${SUFFIX},$(addprefix ${BINDIR},${CMDS}))

.PHONY: all build clean coverage lint test

all: build

build: ${TARGETS}

bin/%: pkg/**/*.go
	@echo "Building $@..."
	go build $(GOFLAGS) -o $@ $(PKG)/cmd/$(subst $(SUFFIX),,$(@:bin/%=%))

coverage: coverage.out

coverage.out: pkg/**/*_test.go
	@echo "Running unit tests with coverage..."
	go test -coverprofile=coverage.out ./pkg/...
	go tool cover -html=coverage.out

lint:
	go vet ./...
	golint -set_exit_status ./...

test:
	@echo "Running unit tests..."
	go test ./pkg/...

clean:
	rm -f ${TARGETS}
