package com.iptech.gradle.unity.internal

import com.iptech.gradle.unity.api.MirrorSpec
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

import javax.inject.Inject

class DefaultMirrorSpec implements MirrorSpec {
    private final Project project

    FileTree fileTree
    File sourceDir
    File destDir
    private final PatternFilterable preserveInDestination = new PatternSet()
    private final PatternFilterable includeInCopy = new PatternSet()

    @Inject
    DefaultMirrorSpec(Project project) {
        this.project = project
    }

    void from(Object srcDir) {
        if(srcDir instanceof FileTree) {
            fileTree = srcDir
        } else {
            sourceDir = project.file(srcDir)
        }
    }

    void into(Object destDir) {
        this.destDir = project.file(destDir)
    }

    PatternFilterable getIncludeInCopy() {
        return this.includeInCopy
    }

    @Override
    MirrorSpec include(Iterable<String> includes) {
        this.includeInCopy.include(includes)
        return this
    }

    @Override
    MirrorSpec preserve(Action<? super PatternFilterable> action) {
        action.execute(this.preserveInDestination)
        return this
    }

    PatternFilterable getPreserveInDestination() {
        return this.preserveInDestination
    }
}
