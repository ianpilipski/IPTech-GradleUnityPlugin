package com.iptech.gradle.unity.tasks

import com.iptech.gradle.unity.UnityExtension
import com.iptech.gradle.unity.api.UnityExecSpec
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

@CompileStatic
class ValidateConfig extends DefaultTask {
    @Nested
    final Property<UnityExtension> unityExtension = project.objects.property(UnityExtension)

    @TaskAction
    void execute() {
        UnityExtension config = this.unityExtension.get()
        println "Validating Unity Plugin Configuration"
        LogAndAssertNotNullOrEmpty(config.unityProductName, 'unityProductName')
        LogAndAssertNotNullOrEmpty(config.unityCmdPath, 'unityCmdPath')
        ValidateUserNamePassword(config)
        LogAndAssertNotNullOrEmpty(config.appVersion.toString(), 'appVersion')
        //TODO: is this required?
        LogAndAssertNotNullOrEmpty(config.buildNumber, 'buildNumber')
        LogAndAssertNotNullOrEmpty(config.mirroredPathRoot, 'mirroredPathRoot')
        LogAndAssertNotNullOrEmpty(config.mirroredUnityProject, 'mirroredUnityProject')
        AssertUnityExecutableExists(project, config.unityCmdPath)
    }

    static void ValidateUserNamePassword(UnityExtension config) {
        println "== unityUserName = ${config.unityUserName}"
        println "== unityPassword = ${config.unityPassword ? '(hidden)' : 'null'}"
        if(!config.unityUserName || !config.unityPassword) {
            throw new GradleException(
                    "\n\nYou must supply a unity username/password in your ~/.gradle/gradle.properties file:\n\n" +
                            "UNITY_USERNAME=<yourunitylogin@youremail.com>\n" +
                            "UNITY_PASSWORD=<yourunitypassword>\n"
            )
        }
    }

    static void LogAndAssertNotNullOrEmpty(String value, String propertyName) {
        println "== ${propertyName} = ${value}"
        AssertNotNullOrEmpty(value, propertyName)
    }

    static void AssertNotNullOrEmpty(String value, String propertyName) {
        if(!(value?.trim())) {
            throw new GradleException("The ${propertyName} is not set to a value.. this is required.")
        }
    }

    static void AssertUnityExecutableExists(Project project, String unityCmdPath) {
        File file = project.file(unityCmdPath)
        if(!file.exists()) {
            throw new GradleException("Unity executable not found at ${unityCmdPath}, please correct and try again.")
        }
    }
}
