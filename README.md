# VariantsStubsGenerator - for android builds with multiple flavors
============

Field and method binding for Android views which uses annotation processing to generate boilerplate
code for you.

Avoid the 'pleasure' of creating and updating stub classes for flavor-specific-functionality.
Annotate flavor-specific-classes to generate stubs on build (no reflection).

For example, for annotated class FlavorSpecificFunctionality
```java
@RequiresVariantStub(flavorFrom = "flavor2", flavorTo = "flavor1")
class FlavorSpecificFunctionality {
  
  public void someFlavorSpecificMethod(int x, int y) {
    ...
  }

```

Download
--------

Configure your project-level `build.gradle` to include the 'android-apt' plugin:

```groovy
buildscript {
  repositories {
    mavenCentral()
   }
  dependencies {
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
  }
}
```

Then, apply the 'android-apt' plugin in your module-level `build.gradle` and add the Butter Knife
dependencies:

```groovy
apply plugin: 'android-apt'

android {
  ...
}

dependencies {
  compile 'com.jakewharton:butterknife:8.4.0'
  apt 'com.jakewharton:butterknife-compiler:8.4.0'
}
```
