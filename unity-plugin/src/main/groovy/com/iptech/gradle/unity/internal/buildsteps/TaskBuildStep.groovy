package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.xcode.tasks.TestFlightUpload
import com.iptech.gradle.xcode.tasks.TestFlightValidate
import org.gradle.api.Task

class TaskBuildStep implements BuildStep {

    @Override
    Boolean getIsTestTask() {
        return false
    }

    @Override
    Iterable<String> getNames() {
        return ['task']
    }

    Task task(String taskPrefix, BuildConfig buildConfig, Closure configClosure=null) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix)
        if(configClosure) {
            t.configure(configClosure);
        }
        return t
    }

    Task task(String taskPrefix, BuildConfig buildConfig, String taskName, Closure configClosure=null) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix + taskName)
        if(configClosure) {
            t.configure(configClosure);
        }
        return t
    }

    Task task(String taskPrefix, BuildConfig buildConfig, String taskName, Class<? super Task> type, Closure configClosure=null) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix + taskName, type)
        if(configClosure) {
            t.configure(configClosure);
        }
        return t
    }
}
