## Start app local
Make sure Docker Compose is installed.
```bash
docker-compose up -d
```

## Test (Kaocha)
Make sure [clj](https://clojure.org/guides/install_clojure) is installed:
```bash
clj -M:test :integration
clj -M:test :unit
```

## Interactive REPL
1. Start a REPL and evaluate the content in `dev/dev.clj`
2. Run the following commands in the REPL (***OPTIONAL***: create a shortcut in EMACS-INTELLIJ-NEOVIM):
```clojure
(in-ns 'dev)
(component-repl/reset)
```
