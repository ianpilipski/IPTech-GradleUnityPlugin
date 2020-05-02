package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.xcode.tasks.ExportArchive
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
        Closure config
    ) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix, ExportArchive) {
            archivePath = buildConfig.xcodeArchivePath
            exportPath = buildConfig.buildDirectory.dir("${taskPrefix}")
        }

        return config==null ? t :  t.configure(config)
    }
}
