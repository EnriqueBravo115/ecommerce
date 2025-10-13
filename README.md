## Interactive REPL
Para ejecutar el proyecto usar Clj -A:dev lo que te pondra en el ns dev, luego en un REPL insertar:
```clojure
(component-repl/reset)
```

## Build jar
Para generar un jar ejecutar en terminal:
```bash
clj -T:build uber
```
Y para correr el jar usar:
```
clj -M -m api.core
```
