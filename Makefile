PKG := synyx.de/tales
CMDS := tales-server

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

.PHONY: all build clean

all: build

build: ${TARGETS}

bin/%:
	@echo "Building $@..."
	go build $(GOFLAGS) -o $@ $(PKG)/cmd/$(subst $(SUFFIX),,$(@:bin/%=%))

clean:
	rm -f ${TARGETS}
