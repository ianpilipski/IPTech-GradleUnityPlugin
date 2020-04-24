package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.xcode.tasks.ExportArchive
import com.iptech.gradle.xcode.tasks.XcodeBuild
import org.gradle.api.Task

class ExportXcodeArchive implements BuildStep {
    @Override
    Boolean getIsTestTask() {
        return false
    }

    @Override
    Iterable<String> getNames() {
        return ['exportXcodeArchive']
    }

    Task exportXcodeArchive(
        String taskPrefix, BuildConfig buildConfig,
        String arg_archivePath, String arg_exportOptionsPlist,
        String arg_exportPath
    ) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix, ExportArchive.class) {
            archivePath arg_archivePath
            exportOptionsPlist arg_exportOptionsPlist
            exportPath arg_exportPath
        }
    }
}
