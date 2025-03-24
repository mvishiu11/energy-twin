All agent code is defined in [energy-twin](/agent-system/energy-twin/).

Some reminders:

To see OpenAPI docs after starting the API go to `http://localhost:8081/v3/api-docs`

To see Swagger UI after starting the API go to `http://localhost:8081/swagger-ui/index.html`

To run formatter use:

```bash
mvn spotless:apply
```

NOTE: To get a JADE dependency locally either uncomment the local path in [pom.xml](/agent-system/energy-twin/pom.xml) or (preferably) install its JAR in you local repository by running:

```bash
mvn install:install-file -Dfile=lib/jade.jar -DgroupId=com.tilab.jade -DartifactId=jade -Dversion=4.6.0 -Dpackaging=jar
```

from `pom.xml` level.