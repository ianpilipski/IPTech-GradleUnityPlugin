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

    Task installProvisioningProfiles(String taskPrefex, BuildConfig buildConfig, Closure config =null) {
        def proj = buildConfig.unity.project

        return proj.tasks.create(taskPrefex) { Task t ->
            ext.from = 'provisioning-profiles'
            t.inputs.dir(proj.provider({ext.from}))
            doLast {
                buildConfig.unity.project.xcode.unInstallProvisioningProfiles {
                    provisioningProfiles = proj.fileTree(t.from) { include '*.mobileprovision.remove' }
                }
                buildConfig.unity.project.xcode.installProvisioningProfiles {
                    provisioningProfiles = proj.fileTree(t.from) { include '*.mobileprovision' }
                }
            }
        }.configure(config?:{})
    }



}
