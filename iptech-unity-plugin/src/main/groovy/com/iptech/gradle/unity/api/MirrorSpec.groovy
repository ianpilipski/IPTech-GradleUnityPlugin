package com.iptech.gradle.unity.api

import org.gradle.api.Action
import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.util.PatternFilterable

interface MirrorSpec {
    void from(Object file)
    void into(Object file)

    MirrorSpec include(String... includes);

    MirrorSpec include(Iterable<String> includes);

    MirrorSpec include(Spec<FileTreeElement> includeSpec);

    MirrorSpec include(Closure includeSpec);

    MirrorSpec preserve(Action<? super PatternFilterable> action)
}
