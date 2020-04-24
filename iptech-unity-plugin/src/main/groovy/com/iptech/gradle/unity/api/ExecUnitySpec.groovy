package com.iptech.gradle.unity.api

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

interface ExecUnitySpec {
    @InputFile abstract RegularFileProperty getUnityCmdPath()
    @Input abstract ListProperty<String> getArguments()
    @Internal abstract DirectoryProperty getProjectPath()
    @Input @Optional abstract Property<String> getBuildTarget()
    @Input @Optional abstract Property<Boolean> getIgnoreExitValue()
    @Input @Optional abstract Property<String> getUserName()
    @Input @Optional abstract Property<String> getPassword()
    @OutputFile @Optional abstract RegularFileProperty getLogFile()
    @Input @Optional abstract MapProperty<String,String> getEnvironment()
}
