## Build jar
Tener instalado [clj](https://clojure.org/guides/install_clojure):
```bash
# Ejectuar el docker-compose
docker-compose up -d
# Construir jar
clj -T:build uber
# Ejecutar jar
java -jar target/ecommerce-api-1.0.0.jar
```

## Test(Kaocha)
Tener instalado [clj](https://clojure.org/guides/install_clojure):
```bash
clj -M:test :integration
clj -M:test :unit
```

## Interactive REPL
1. Iniciar un repl y evaluar el contenido en `dev/dev.clj`
2. Ejecutar los siguientes comandos en el repl(***OPCIONAL***: crear un atajo):
```clojure 
(in-ns 'dev)
(component-repl/reset)
```
