package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.xcode.tasks.Archive
import javafx.beans.property.ListProperty
import org.gradle.api.Task
import com.iptech.gradle.xcode.tasks.XcodeBuild
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
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

    Task archiveXcodeProject(String taskPrefix, BuildConfig buildConfig, Closure configClosure) {
        buildConfig.ext.xcodeArchivePath = buildConfig.buildDirectory.dir("${taskPrefix}/xcode-archive.xcarchive")

        Task t = buildConfig.unity.project.tasks.create(taskPrefix, Archive) {
            projectPath = buildConfig.unityBuildOutput.dir("Unity-iPhone.xcodeproj")
            configuration = 'Release'
            archivePath = buildConfig.xcodeArchivePath
            scheme = 'Unity-iPhone'
            CODE_SIGNING_ALLOWED = 'NO'
            CODE_SIGNING_REQUIRED = 'NO'
            PROVISIONING_PROFILE_SPECIFIER = ''
            CODE_SIGN_IDENTITY = ''
            DEVELOPMENT_TEAM = ''
        }
        if(configClosure) {
            t.configure(configClosure)
        }
        return t
    }
}
