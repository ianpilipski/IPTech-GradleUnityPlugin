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
        return buildConfig.unity.project.tasks.create(taskPrefex) {
            doLast {
                buildConfig.unity.project.xcode.unInstallProvisioningProfiles {
                    provisioningProfiles = buildConfig.unity.project.fileTree( 'provisioning-profiles') { include '*.mobileprovision.remove' }
                }
                buildConfig.unity.project.xcode.installProvisioningProfiles {
                    provisioningProfiles = buildConfig.unity.project.fileTree( 'provisioning-profiles') { include '*.mobileprovision' }
                }
            }
        }.configure(config?:{})
    }

}
