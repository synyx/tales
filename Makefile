PKG := synyx.de/tales
CMD := tales-server

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
TARGET=$(addsuffix ${SUFFIX},$(addprefix ${BINDIR},${CMD}))

.PHONY: all build clean coverage lint test ${TARGET}

all: build

build: ${TARGET}

${TARGET}:
	@echo "Building $@..."
	go build $(GOFLAGS) -o $@ $(PKG)/cmd/$(subst $(SUFFIX),,$(@:bin/%=%))

coverage:
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
	rm -f ${TARGET}
