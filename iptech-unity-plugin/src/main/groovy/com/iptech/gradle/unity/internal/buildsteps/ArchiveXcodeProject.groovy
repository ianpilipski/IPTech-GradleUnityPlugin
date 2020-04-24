package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.xcode.tasks.Archive
import org.gradle.api.Task
import com.iptech.gradle.xcode.tasks.XcodeBuild
import org.gradle.util.ConfigureUtil

class ArchiveXcodeProject implements BuildStep {

    @Override
    Boolean getIsTestTask() {
        return false
    }

    @Override
    Iterable<String> getNames() {
        return ['archiveXcodeProject']
    }

    Task archiveXcodeProject(String taskPrefix, BuildConfig buildConfig, String pPath, String ddPath, Closure configClosure) {
        Closure preConfig = {
            project pPath
            derivedDataPath ddPath
            configuration 'Release'
            scheme 'Unity-iPhone'
            propCODE_SIGNING_ALLOWED 'NO'
            propCODE_SIGNING_REQUIRED 'NO'
            propPROVISIONING_PROFILE_SPECIFIER ''
            propCODE_SIGN_IDENTITY ''
            propDEVELOPMENT_TEAM ''

            inputs.dir pPath
            outputs.dir ddPath
        }

        Closure postConfig = {
            additionalArguments(['clean', 'archive'])
        }

        Closure config = preConfig >> (configClosure?:{}) >> postConfig
        return buildConfig.unity.project.tasks.create(taskPrefix, Archive.class, config)
    }
}
