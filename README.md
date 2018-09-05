# Glassfish CORBA ORB

This is the [glassfish-corba project](https://javaee.github.io/glassfish-corba/).
 
## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```
