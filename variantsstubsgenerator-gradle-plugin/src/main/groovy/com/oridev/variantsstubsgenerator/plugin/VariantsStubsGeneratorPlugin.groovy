package com.oridev.variantsstubsgenerator.plugin

import com.sun.codemodel.internal.JCodeModel
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.tooling.model.Task

public class VariantsStubsGeneratorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

    }

    void rewriteGeneratedFiles(Project project) {
        def buildDir = project.buildDir

        project.android.applicationVariants.all { variant ->

            Task task = new RewriteFilesTask();
            variant.registerResGeneratingTask(task, variant.name)
        }
        def generatedFilesJson = new File( )
    }


    private String getGeneratedJsonPath(String variant) {
        return buildDir.absolutePath + "/generated/source/apt/$variant";
    }

    class RewriteFilesTask extends DefaultTask {
        @OutputDirectory
        File outputDir

        @Input
        String flavor
    }

}
