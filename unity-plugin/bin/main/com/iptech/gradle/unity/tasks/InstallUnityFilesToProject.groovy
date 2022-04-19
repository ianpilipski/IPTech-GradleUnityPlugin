package com.iptech.gradle.unity.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskAction

class InstallUnityFilesToProject extends Sync {
    InstallUnityFilesToProject() {
        from project.layout.buildDirectory.dir('tmp/unity-files/IPTech.UnityGradlePlugin')
        into project.unity.projectPath.dir('Assets/Plugins/IPTech.UnityGradlePlugin')
    }
}
