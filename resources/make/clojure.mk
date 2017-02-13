$(LOG_DIR):
	@mkdir -p $(LOG_DIR)

clojure-build: $(LFE_DEPLOY_DIR) $(LOG_DIR)
	@lein compile
	@lein uberjar
	mv target/*.jar $(LFE_DEPLOY_DIR)

clojure-clean:
	@lein clean

clojure-repl:
	@lein with-profile +app repl

clojure-server:
	@lein with-profile +app run
