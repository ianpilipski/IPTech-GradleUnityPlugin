package com.iptech.gradle.unity.internal

import com.iptech.gradle.unity.api.BuildStep

class BuildStepManager {
    final Map<String, NewBuildStep> registeredBuildStepFuncs = [:]

    void registerBuildStep(BuildStep buildStep) {
        buildStep.names.each {
            registerBuildStepFunction(it, buildStep.&"$it", buildStep.isTestTask)
        }
    }

    Boolean hasBuildStep(String name) {
        return registeredBuildSteps.containsKey(name)
    }

    void registerBuildStepFunction(String name, Closure func, Boolean isTestTask = false) {
        println "registering build step function $name"
        registeredBuildStepFuncs.put(name, new NewBuildStep(func, isTestTask))
    }

    class NewBuildStep {
        Boolean isTestTask
        Closure func

        NewBuildStep(Closure func, Boolean isTestTask) {
            this.func = func
            this.isTestTask = isTestTask
        }
    }
}
