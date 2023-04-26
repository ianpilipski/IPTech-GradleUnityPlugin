package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.xcode.tasks.TestFlightUpload
import com.iptech.gradle.xcode.tasks.TestFlightValidate
import org.gradle.api.Task
import java.io.FileOutputStream

class TaskBuildStep implements BuildStep {

    @Override
    Boolean getIsTestTask() {
        return false
    }

    @Override
    Iterable<String> getNames() {
        return ['task' ]
    }

    Task task(String taskPrefix, BuildConfig buildConfig, Closure configClosure=null) {
        return task(taskPrefix, buildConfig, null, null, configClosure)
    }

    Task task(String taskPrefix, BuildConfig buildConfig, String taskName, Closure configClosure=null) {
        return task(taskPrefix, buildConfig, taskName, null, configClosure)
    }

    Task task(String taskPrefix, BuildConfig buildConfig, String taskName, Class<? super Task> type, Closure configClosure=null) {
        def p = buildConfig.unity.project
        String tName = taskPrefix + (taskName ?: "");
        Task t = type==null ? p.tasks.create(tName) : p.tasks.create(tName, type)
        t.ext.outputDir = "${p.buildDir}/unity/${tName}"

        if(t instanceof org.gradle.api.tasks.Exec) {
            t.doFirst {
                p.mkdir(outputDir)
            }
            t.workingDir = t.ext.outputDir
            t.standardOutput = new FileOutputStream("${t.ext.outputDir}/output.log")
        }

        if(configClosure) {
            t.configure(configClosure);
        }
        return t
    }
}
