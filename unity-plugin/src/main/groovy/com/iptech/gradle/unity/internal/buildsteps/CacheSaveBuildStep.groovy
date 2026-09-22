package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.tasks.CacheSave
import org.gradle.api.Task

class CacheSaveBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['cacheSave']
    }

    @Override
    Boolean getIsTestTask() {
        return false
    }

    Task cacheSave(String taskPrefix, BuildConfig buildConfig, Closure configClosure=null) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix, CacheSave)
        t.buildConfig.set(buildConfig)
        if(configClosure) {
            t.configure(configClosure)
        }
        return t
    }
}
