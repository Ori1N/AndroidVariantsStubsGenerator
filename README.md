# VariantsStubsGenerator
============

Library for android projects using gradle with multiple flavors on AndroidStudio (not eclipse).

Annotate flavor-specific-classes on multi-flavored android projects to 
create build-generated stub classes.

Avoid the 'pleasure' of creating and updating stub classes for flavor-specific-functionality.
Annotate flavor-specific-classes to generate stubs classes containing all the public methods
of the annotated class.
The stubs are generated when building the variant containing the annotation
(no reflection)

For example, for annotated class FlavorSpecificFunctionality
```java
@RequiresVariantStub(flavorFrom = "flavorExtraFunctionality", flavorTo = "flavorRegular")
class ExtraFunctionality {
  
  public void someFunctionality(int x, int y) {
    ...
  }
```

will generate the following stub file under build/generated/source/apt/flavorRegular/{package}/ExtraFunctionality.java
```java
class ExtraFunctionality {

  public void someFunctionality(int x, int y) {
  }
```

So you can call `ExtraFunctionality.someFunctionality(testX, testY)` 
from the main source set and the application will compile also on `flavorRegular`

* Android Studio recognizes the generated files and doesn't raise errors.

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
