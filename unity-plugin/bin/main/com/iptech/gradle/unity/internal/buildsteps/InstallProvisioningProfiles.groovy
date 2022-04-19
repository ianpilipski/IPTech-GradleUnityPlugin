package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import org.gradle.api.Task
import com.iptech.gradle.xcode.tasks.InstallProvisioningProfiles as XcodeInstallProvisioningProfiles

class InstallProvisioningProfiles implements BuildStep {

    @Override
    Boolean getIsTestTask() {
        return false
    }

    @Override
    Iterable<String> getNames() {
        return ['installProvisioningProfiles']
    }

    Task installProvisioningProfiles(String taskPrefex, BuildConfig buildConfig, Closure config ) {
        return buildConfig.unity.project.tasks.create(taskPrefex, XcodeInstallProvisioningProfiles) {
            provisioningProfiles = buildConfig.unity.project.fileTree( 'profiles') { include '*.mobileprovision' }
        }.configure(config?:{})
    }
}
