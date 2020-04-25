package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider

class InstallProvisioningProfiles implements BuildStep {

    @Override
    Boolean getIsTestTask() {
        return false
    }

    @Override
    Iterable<String> getNames() {
        return ['installProvisioningProfiles']
    }

    Task installProvisioningProfiles(String taskPrefex, BuildConfig buildConfig, Provider<FileCollection> provProfiles) {
        return buildConfig.unity.project.tasks.create(taskPrefex, com.iptech.gradle.xcode.tasks.InstallProvisioningProfiles) {
            provisioningProfiles = provProfiles
        }
    }
}
