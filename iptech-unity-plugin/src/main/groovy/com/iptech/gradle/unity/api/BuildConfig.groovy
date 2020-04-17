package com.iptech.gradle.unity.api

import com.iptech.gradle.unity.UnityExtension
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.process.ExecResult

import javax.inject.Inject

class BuildConfig {
    @Internal final UnityExtension unity

    @Input String name
    @Input String platform
    @OutputDirectory @Optional String outputPath
    @Internal DomainObjectSet<Closure> steps

    @Inject
    BuildConfig(String name, UnityExtension unity) {
        this.unity = unity
        this.name = name
        this.steps = unity.project.objects.domainObjectSet(Closure.class)
    }

    void steps(Closure stepsClosure) {
        this.steps.add(stepsClosure)
    }

    void platform(String platform) {
        this.platform = platform
    }

    void outputPath(String outputPath) {
        this.outputPath = outputPath
    }

    @Input
    String getUnityPlatform() {
        if(platform=='Amazon') {
            return 'Android'
        }
        return platform
    }

    @OutputDirectory
    File getMirrordProjectPath() {
        return unity.project.file("${unity.mirroredPathRoot}/${unity.mirroredUnityProject}")
    }

    @OutputDirectory
    File getLogDir() {
        return unity.project.file("${unity.project.buildDir}/${this.name}-${unity.bundleVersion}-${unity.buildNumber}/logs")
    }

    @OutputDirectory
    File getArtifactDir() {
        return unity.project.file( "${unity.project.buildDir}/${this.name}-${unity.bundleVersion}-${unity.buildNumber}/artifacts")
    }


    ExecResult execUnity(Action<? super UnityExecSpec> execSpec) {
        BuildConfig self = this
        return unity.exec(new Action<UnityExecSpec>() {
            @Override
            void execute(UnityExecSpec unityExecSpec) {
                unityExecSpec.projectPath(self.getMirrordProjectPath().path)
                unityExecSpec.buildTarget(self.getUnityPlatform())
                execSpec.execute(unityExecSpec)
            }
        })
    }
}