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

So when executing `./gradlew assembleFreeDebug` a stub class will be generated 
 to `app/build/generated/source/flavors/free/com.oridev.variantsstubsgenerator.sample.utils.PaidFunctionality.java`
```java
// Generated stub. Do not modify!
class PaidFunctionality {

  public void getPaidMessage(Context context) {
    return null;
  }
}
```

So you can call `PaidFunctionality.getPaidMessage(context)` 
from the main source set and flavor `free` will compile successfully.

* Android Studio recognizes the generated files.


Download
--------

Configure your project-level `build.gradle` to include the plugin:

```groovy
buildscript {
  repositories {
    mavenCentral()
   }
  dependencies {
    ...
  
    classpath 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-plugin:0.4.5'
  }
}
```

Then, apply the plugin in your module-level `build.gradle` and add the annotation dependency:

```groovy
... 

dependencies {
    ...
    
    compile 'com.oridev.variantsstubsgenerator:variantsstubsgenerator-annotation:0.4.5'
}

apply plugin: 'variantsstubsgenerator'

```

If you are using proguard you should also add the following configuration to your proguard rules:
```proguard
-dontwarn com.oridev.variantsstubsgenerator.plugin.**
```



Happy coding!