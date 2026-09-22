package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.tasks.CacheRestore
import org.gradle.api.Task

class CacheRestoreBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['cacheRestore']
    }

    @Override
    Boolean getIsTestTask() {
        return false
    }

    Task cacheRestore(String taskPrefix, BuildConfig buildConfig, Closure configClosure=null) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix, CacheRestore)
        t.buildConfig.set(buildConfig)
        if(configClosure) {
            t.configure(configClosure)
        }
        return t
    }
}
