package com.iptech.gradle.unity.tasks

import com.iptech.gradle.unity.UnityExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.file.DefaultFilePropertyFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable

class ValidateConfig extends DefaultTask {
    @Nested
    final Property<UnityExtension> unity = project.objects.property(UnityExtension)

    @TaskAction
    void execute() {
        UnityExtension config = this.unity.get()
        println "Validating Unity Plugin Configuration"

        LogAndAssertNotNullOrEmpty(config.productName             , 'productName')
        LogAndAssertNotNullOrEmpty(config.bundleVersion           , 'bundleVersion')
        LogAndAssertNotNullOrEmpty(config.unityCmdPath            , 'unityCmdPath')
        ValidateUserNamePassword(config)
        LogAndAssertNotNullOrEmpty(config.buildNumber             , 'buildNumber')
        LogAndAssertNotNullOrEmpty(config.buildCachePath          , 'buildCachePath')
        LogAndAssertNotNullOrEmpty(config.unityProjectFilter      , 'unityProjectFilter')

        AssertUnityExecutableExists(project, config.unityCmdPath)
    }

    static void ValidateUserNamePassword(UnityExtension config) {
        println "== unity.userName = ${config.userName.getOrNull()}"
        println "== unity.password = ${config.password.getOrNull() ? '(hidden)' : 'null'}"
    }

    static void LogAndAssertNotNullOrEmpty(Provider<? super Object> provider, String propertyName) {
        if(!provider.isPresent()) {
            AssertNotNullOrEmpty("", propertyName)
        } else {
            Object value = provider.get()
            if(value instanceof FileSystemLocation) {
                value = value.asFile.absolutePath
            } else if(value instanceof PatternFilterable) {
                value = ((PatternFilterable)value).getIncludes().toString()
            }
            if(value instanceof String) {
                println "== unity.${propertyName} = ${value}"
                AssertNotNullOrEmpty(value, propertyName)
            } else {
                println "== unity.${propertyName} = <${value.class.simpleName}>"
            }
        }
    }

    static void AssertNotNullOrEmpty(String value, String propertyName) {
        if(!(value?.trim())) {
            throw new GradleException("The unity.${propertyName} is not set to a value.. this is required.")
        }
    }

    static void AssertUnityExecutableExists(Project project, RegularFileProperty unityCmdPath) {
        File file = unityCmdPath.get().asFile
        if(!file.exists()) {
            throw new GradleException("Unity executable not found at ${unityCmdPath}, please correct and try again.")
        }
    }
}
