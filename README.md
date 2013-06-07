Clirr Gradle Plugin
===================

Plugin allows you to use Clirr from Gradle.

Usage
-----

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'me.trnl:clirr-gradle-plugin:0.1'
    }
}

clirr {
    ignoreFailures = true
    ignoredDifferenceTypes 10000
    ignoredPackages 'org.mongodb'
    baseline 'org.mongodb:mongo-java-driver:2.11.1'
}
```

Reports
-------
Plugins provides reports in XML and HTML formats

![HTML Report](http://f.cl.ly/items/1A0r1J3O2G1a0z421Q29/Image%202013.06.07%203%3A19%3A46%20PM.png)

Difference Types
----------------

- 1000 (Increased visibility of a class): className
- 1001 (Decreased visibility of a class): className
- 2000 (Changed from class to interface): className
- 2001 (Changed from interface to class): className
- 3001 (Removed final modifier from class): className
- 3002 (Added final modifier to effectively final class): className
- 3003 (Added final modifier to class): className
- 3004 (Removed abstract modifier from class): className
- 3005 (Added abstract modifier to class): className
- 4000 (Added interface to the set of implemented interfaces): className, to (as a path expression)
- 4001 (Removed interface from the set of implemented interfaces): className, to (as a path expression)
- 5000 (Added class to the set of superclasses): className, to (as a path expression)
- 5001 (Removed class from the set of superclasses): className, to (as a path expression)
- 6000 (added field): className, field
- 6001 (removed field): className, field
- 6002 (field value no longer a compile-time constant): className, field
- 6003 (value of the compile-time constant changed on a field): className, field
- 6004 (field type changed): className, field, from, to
- 6005 (field now non-final): className, field
- 6006 (field now final): className, field
- 6007 (field now non-static): className, field
- 6008 (field now static): className, field
- 6009 (field more accessible): className, field
- 6010 (field less accessible): className, field
- 6011 (removed a constant field): className, field
- 7000 (method now in superclass): className, method
- 7001 (method now in interface): className, method
- 7002 (method removed): className, method
- 7003 (Method Overide Removed): className, method
- 7004 (Method Argument Count Changed): className, method
- 7005 (Method Argument Type changed): className, method, to (to is a full new signature)
- 7006 (Method Return Type changed): className, method, to (to is just the return type)
- 7007 (Method has been Deprecated): className, method
- 7008 (Method has been Undeprecated): className, method
- 7009 (Method is now Less Accessible): className, method
- 7010 (Method is now More Accessible): className, method
- 7011 (Method Added): className, method
- 7012 (Method Added to Interface): className, method
- 7013 (Abstract Method Added to Class): className, method
- 7014 (Method now final): className, method
- 7015 (Method now non-final): className, method
- 8000 (Class added): className
- 8001 (Class removed): className
- 10000 (Class format version increased): className, from, to (class format version numbers, NOT expressions)
- 10001 (Class format version decreased): className, from, to (class format version numbers, NOT expressions)


