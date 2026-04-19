# Glassfish CORBA ORB

[Eclipse GlassFish CORBA-ORB](https://projects.eclipse.org/projects/ee4j.orb) is a runtime component that can be used
for distributed computing using IIOP communication.

Compatibility:

| GlassFish CORBA-ORB version                                           | Java    | Notes                                      |
| --------------------------------------------------------------------- | ------- | ------------------------------------------ |
| [6.0](https://github.com/eclipse-ee4j/orb/milestone/9)                | 17 - 26 | TBD, Removed Obsoleted JDK API usages      |
| [5.0](https://github.com/eclipse-ee4j/orb/releases/tag/5.0.0)         | 17 - 25 | JPMS support                               |
| [4.2](https://github.com/eclipse-ee4j/orb/releases/tag/4.2.5-RELEASE) |  8 - 11 | Migrated from Oracle to Eclipse Foundation |
 
Note: Compatibility means that the project passes its own tests running `mvn clean install`.

## Build

Run all tests

* Not supported on JDK25+ until we remove Applets and SecurityManager

```
mvn -Pall-tests clean install
```

## Release

* For a temporary release branch
* Use [CI Release Job](https://ci.eclipse.org/orb/view/Release/job/release-and-deploy) to build, test and deploy
* Update this file, README.md

## Documentation

Please visit the [www](www) and [www/design](www/design) directories. Unfortunately we did not migrate the project
website yet, but most of it is in these files. You can also visit the original 
[Java EE Oracle GlassFish CORBA](https://javaee.github.io/glassfish-corba/index.html) website.
