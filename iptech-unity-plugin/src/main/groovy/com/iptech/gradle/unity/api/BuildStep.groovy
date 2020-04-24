package com.iptech.gradle.unity.api

import org.gradle.api.Task

interface BuildStep {
    Boolean getIsTestTask()
    Iterable<String> getNames()
}