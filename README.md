# VariantsStubsGenerator
============

Library for android projects using gradle with multiple flavors on Android Studio (not eclipse).

Annotate flavor-specific-classes on multi-flavored android projects to 
create build-generated stub classes.

Avoid the 'pleasure' of creating and updating stub classes for flavor-specific-functionality.
For each annotated class a java stub file will be generated on build (no reflection), containing 
all the public methods of the annotated class.
This file will be generated to the flavor specified in the annotation (parameter `flavorTo`).

For example, in case you have `free` and `paid` flavors in your app and you want to add some extra 
functionality for the paid version, then you can add the extra functionality to `app/src/paid/com.example.PaidFunctionality`
and use the annotation as follows:
```java
@RequiresVariantStub(flavorTo = "free")
class PaidFunctionality {
  
  public void someFunctionality(int x, int y) {
    ...
  }
}
```

So when executing `./gradlew assemblePaid` a stub class will be generated 
 to `app/build/generated/source/apt/free/com.example.PaidFunctionality.java`
```java
class PaidFunctionality {

  public void someFunctionality(int x, int y) {
  }
}
```

So you can call `PaidFunctionality.someFunctionality(testX, testY)` 
from the main source set and flavor `free` will compile successfully.

* Android Studio recognizes the generated files.

Download
--------

Configure your project-level `build.gradle` to include the following plugins:

```groovy
buildscript {
  repositories {
    mavenCentral()
   }
  dependencies {
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    classpath 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-plugin:0.2.0'
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
    ...
    
    compile 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-annotation:0.2.0'
    compile 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-plugin:0.2.0'
    apt 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-compiler:0.2.0'
}

apply plugin: 'variantsstubsgenerator'

```
