# CLI [![Build Status](https://travis-ci.org/SpartanRefactoring/cli.svg?branch=master)](https://travis-ci.org/SpartanRefactoring/cli) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/il.org.spartan/cli/badge.svg)](https://maven-badges.herokuapp.com/maven-central/il.org.spartan/cli/)

CLI enables Java developers to automatically create command line parameters for their code based on the fields detected in the code.

## Obtaining the library
The library is available in Maven Central, so you can easily embed it in your projects.

#### Maven

For Maven projects, add this to your pom.xml:

```
<dependencies>
    <dependency>
        <groupId>il.org.spartan</groupId>
        <artifactId>cli</artifactId>
        <version>1.0</version>
    </dependency>
    ...
</dependencies>
```

#### Gradle

For Gradle projects, add this to your build.gradle:
```
compile 'il.org.spartan:cli:1.0'
```

#### Compiling from source

To compile the library from source, clone this repository to your local computer and run:
```
mvn package
```

Then, include the generated .jar file in your project.

## License

This library is an open source project and is available under the [MIT License](https://opensource.org/licenses/MIT)
