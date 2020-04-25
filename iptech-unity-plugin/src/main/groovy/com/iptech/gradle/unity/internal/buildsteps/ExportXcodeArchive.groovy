package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.xcode.tasks.ExportArchive
import com.iptech.gradle.xcode.tasks.XcodeBuild
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

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
        return buildConfig.unity.project.tasks.create(taskPrefix, ExportArchive.class, config)
    }
}
