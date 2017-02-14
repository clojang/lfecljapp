$(LFE_DEPLOY_DIR):
	@mkdir -p $(LFE_DEPLOY_DIR)

lfe-build: priv
	@rebar3 compile

check:
	rebar3 as test lfe test -t unit

lfe-repl: lfe-build
	@$(LFE) \
	-sname "lfenode@$(HOST)" \
	-pa `rebar3 path -s " -pa "`

lfe-server:
	echo TBD

erl-shell:
	@rebar3 shell

lfe-clean:
	@rebar3 clean
	@rm -rf ebin/* \
	$(LFE_BUILD_DIR)/$(PROJECT) \
	$(LFE_DEPLOY_DIR) \
	rebar.lock erl_crash.dump
