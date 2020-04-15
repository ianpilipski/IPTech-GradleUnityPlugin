package com.iptech.gradle.unity.api

import org.gradle.api.Project
import org.gradle.api.Task

interface BuildStep {
    Iterable<String> getNames()
    Iterable<Task> createTasks(String stepName, String taskPrefix, BuildConfig buildConfig, Object args)
}