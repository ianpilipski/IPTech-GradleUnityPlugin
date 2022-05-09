package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.xcode.tasks.TestFlightUpload
import com.iptech.gradle.xcode.tasks.TestFlightValidate
import org.gradle.api.Task

class TestFlightSteps implements BuildStep {

    @Override
    Boolean getIsTestTask() {
        return false
    }

    @Override
    Iterable<String> getNames() {
        return ['uploadToTestFlight', 'validateTestFlight']
    }

    Task uploadToTestFlight(String taskPrefix, BuildConfig buildConfig, Closure configClosure) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix, TestFlightUpload) {
            it.password = buildConfig.unity.applePassword
            it.userName = buildConfig.unity.appleUserName
            it.appType = 'ios'
            //it.appFile
        }
        if(configClosure) {
            t.configure(configClosure)
        }
        return t
    }

    Task validateTestFlight(String taskPrefix, BuildConfig buildConfig, Closure configClosure) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix, TestFlightValidate) {
            it.password = buildConfig.unity.applePassword
            it.userName = buildConfig.unity.appleUserName
            //it.appFile
            it.appType = 'ios'
        }

        if(configClosure) {
            t.configure(configClosure)
        }
        return t
    }
}
