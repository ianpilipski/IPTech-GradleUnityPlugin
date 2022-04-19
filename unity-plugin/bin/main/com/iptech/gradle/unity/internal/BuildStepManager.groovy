package com.iptech.gradle.unity.internal

import com.iptech.gradle.unity.api.BuildStep

class BuildStepManager {
    private final Map<String, BuildStep> registeredBuildSteps = [:]

    void registerBuildStep(BuildStep buildStep) {
        buildStep.names.each {
            registeredBuildSteps.put(it, buildStep)
        }
    }

    Boolean hasBuildStep(String name) {
        return registeredBuildSteps.containsKey(name)
    }

    BuildStep getBuildStep(String name) {
        return registeredBuildSteps.get(name)
    }
}
