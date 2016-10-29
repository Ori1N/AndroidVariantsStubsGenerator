# VariantsStubsGenerator-Compiler
============

The annotation processor module.

Generates a stub java file for each annotated class, containing all the public elements of the annotated class.
The files are generated into their target path according to their flavors / build-types.
(For example: `app/build/generated/source/apt/debug/free/com.example`

Additionally, an info json is created containing the target-flavor and file-path for each generated file.
The json is created to `app/build/generated/assets/variantsStubsGenerator/meta/generated_files.json`.
This info file is later used by the [plugin](https://github.com/Ori1N/AndroidVariantsStubsGenerator/tree/master/variantsstubsgenerator-plugin), 
which adds the generated files to their right source-sets.