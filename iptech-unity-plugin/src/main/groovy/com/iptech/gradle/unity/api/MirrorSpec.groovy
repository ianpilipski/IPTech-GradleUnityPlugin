package com.iptech.gradle.unity.api

import org.gradle.api.Action
import org.gradle.api.tasks.util.PatternFilterable

interface MirrorSpec {
    void from(Object file)
    void into(Object file)
    MirrorSpec include(Iterable<String> includes);
    MirrorSpec preserve(Action<? super PatternFilterable> action)
}
