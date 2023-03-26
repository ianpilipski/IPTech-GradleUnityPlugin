package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import org.gradle.api.Task
import org.gradle.api.tasks.Exec

class BuildGradleProject implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['buildGradleProject']
    }

    @Override
    Boolean getIsTestTask() {
        return false
    }

    Task buildGradleProject(String taskPrefix, BuildConfig buildConfig, Closure configClosure=null) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix, Exec) {
            workingDir buildConfig.unityBuildOutput
            executable '/bin/bash'
            args '-l', '-f', 'gradlew', 'build', 'bundle'
        }
        t.configure(configClosure?:{})
        return t
    }
}
