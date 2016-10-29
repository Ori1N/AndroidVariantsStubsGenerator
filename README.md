# VariantsStubsGenerator
============

Library for android projects using gradle with multiple flavors on Android Studio (not eclipse).

Annotate flavor-specific-classes on multi-flavored android projects to 
create build-generated stub classes.

Avoid the 'pleasure' of creating and updating stub classes for flavor-specific-functionality.
For each annotated class a java stub file will be generated on build (no reflection), containing 
all the public methods of the annotated class.
This file will be generated to the flavor specified in the annotation (parameter `flavorTo`).

For example, in the sample we have `free` and `paid` flavors and we want to add some extra 
functionality for the paid version, then we can add the extra functionality to `app/src/paid/com.oridev.variantsstubsgenerator.sample.utils.PaidFunctionality`
and use the annotation as follows:
```java
@RequiresVariantStub(flavorTo = "free")
class PaidFunctionality {
  
  public String getPaidMessage(Context context) {
    ...
  }
}
```

So when executing `./gradlew assemblePaidDebug` a stub class will be generated 
 to `app/build/generated/source/apt/debug/free/com.oridev.variantsstubsgenerator.sample.utils.PaidFunctionality.java`
```java
class PaidFunctionality {

  public void getPaidMEssage(Context context) {
    return null;
  }
}
```

So you can call `PaidFunctionality.getPaidMessage(context)` 
from the main source set and flavor `free` will compile successfully.

* Android Studio recognizes the generated files.

Usage notes:
- The library's gradle plugin must be applied after building the variant containing generating
elements before the variant containing the generated elements can be built successfully, meaning 
that building both variants in the same command **won't work!**:
```groovy
// won't work in the same command!!
./gradlew assemblePaidDebug assembleFreeDebug
```
- If you see the following warning on build, that's normal, and should be ignored:
<br/>`warning: Unclosed files for the types '[com.example._d_]'; these types will not undergo annotation processing`



Download
--------

Configure your project-level `build.gradle` to include the following plugins:

```groovy
buildscript {
  repositories {
    mavenCentral()
   }
  dependencies {
    ...
  
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    classpath 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-plugin:0.2.5'
  }
}
```

Then, apply the 'android-apt' plugin in your module-level `build.gradle` and add the library
dependencies:

```groovy
apply plugin: 'android-apt'

dependencies {
    ...
    
    compile 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-annotation:0.2.5'
    apt 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-compiler:0.2.5'
    compile 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-plugin:0.2.5'
}

apply plugin: 'variantsstubsgenerator'

```

If you are using proguard you should also add the following configuration to your proguard rules:
```proguard
-dontwarn com.oridev.variantsstubsgenerator.plugin.**
```



Happy coding!