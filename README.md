#pentaho-metaverse-ee

This project defines implementations and interfaces for use with the Pentaho Metaverse, which provides the following capabilities:

- Data Lineage
- Impact Analysis
- Metadata auditing/governance
- Provenance diagnostics (missing links, references, files, etc.)


### How to build this project
*This is a maven-based project so you must have it available on your system.*

**Typical build.** This will compile the code, run the unit tests, and assemble a Pentaho platform-plugin in the `target` folder.
```
mvn install
```

**Compile only.**
```
mvn compile
```

**Run the unit tests.**
```
mvn test
```

**Run the integration tests.**
```
mvn integration-test
```

**Run the unit tests with code coverage.** This will output an html report here: `target/site/cobertura/index.html`
```
mvn clean compile test-compile cobertura:cobertura
```

**Run checkstyle against the code.** This will output an html report here: `target/site/checkstyle.html`
```
mvn checkstyle:checkstyle
```

**Maven command line switches to get familiar with**

- `-o, --offline` Work offline
- `-D, --define` Defines a system property. One particularly useage of this iw when you don't want the unit tests to run. To do that, just define the skipTests property
```
mvn install -DskipTests
```
- `-U, --update-snapshots` Forces an update for releases and snapshots from remote repository

##### Other build tips
The default profile when building is "production". When that profile is active, the install goal will also obfuscate the artifact. To force this to the development profile where obfuscation is not run, you have 3 options...
- set the active profile on the commandline
```
mvn install -P development
mvn install -P production
```
- activate the profile with the "env" property setting
```
mvn install -Denv=dev
mvn install -Denv=prod
```
- activate the profile in your <user home>/.m2/settings.xml file using the env property
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd">

  <profiles>
    <profile>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      
      <properties>
        <env>dev</env>
      </properties>
    </profile>
  </profiles>
</settings>
```

### Project structure

This project follows the standard maven project layout
```
pentaho-metaverse-ee
├── src
│   ├── assembly               # artifact assembly definition and resources
│   │   └── resources
│   ├── it                     # integration test code and resources
│   │   ├── java
│   │   └── resources
│   ├── main                   # main code and resources
│   │   ├── java
│   │   └── resources
│   ├── site
│   └── test                   # unit test code and resources
│       ├── java
│       └── resources
└── target                     # generated stuff (compiled classes, reports, assembled artifacts, ...)
```

