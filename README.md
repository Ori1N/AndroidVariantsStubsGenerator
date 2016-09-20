# VariantsStubsGenerator
============

Library for android projects using gradle with multiple flavors on Android Studio (not eclipse).

Annotate flavor-specific-classes on multi-flavored android projects to 
create build-generated stub classes.

Avoid the 'pleasure' of creating and updating stub classes for flavor-specific-functionality.
For each annotated class a java stub file will be generated on build (no reflection), containing 
all the public methods of the annotated class.
This file will be generated to the flavor specified in the annotation (parameter `flavorTo`).

For example, for annotated class FlavorSpecificFunctionality
```java
@RequiresVariantStub(flavorFrom = "flavorExtraFunctionality", flavorTo = "flavorRegular")
class ExtraFunctionality {
  
  public void someFunctionality(int x, int y) {
    ...
  }
}
```

will generate the following stub file under `build/generated/source/apt/flavorRegular/{package}/ExtraFunctionality.java`
```java
class ExtraFunctionality {

  public void someFunctionality(int x, int y) {
  }
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

Then, apply the 'android-apt' plugin in your module-level `build.gradle` and add the library
dependencies:

```groovy
apply plugin: 'android-apt'

android {
  ...
}

dependencies {
  // still not published...
}
```
