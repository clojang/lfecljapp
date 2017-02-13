PROJECT = lfecljapp
ROOT_DIR = $(shell pwd)
REPO = $(shell git config --get remote.origin.url)
LFE_BUILD_DIR = _build/default/lib
LFE_DEPLOY_DIR = priv
LOG_DIR = log
LFE = $(LFE_BUILD_DIR)/lfe/bin/lfe
OS := $(shell uname -s)
ifeq ($(OS),Linux)
	HOST=$(HOSTNAME)
endif
ifeq ($(OS),Darwin)
	HOST = $(shell scutil --get ComputerName)
endif

include resources/make/lfe.mk
include resources/make/clojure.mk

compile: lfe-build clojure-build

clean: lfe-clean clojure-clean

push:
	git push github master
	git push gitlab master

push-tags:
	git push github --tags
	git push gitlab --tags

push-all: push push-tags
