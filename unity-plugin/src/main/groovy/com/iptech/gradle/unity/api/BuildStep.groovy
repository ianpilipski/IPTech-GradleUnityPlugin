package com.iptech.gradle.unity.api


interface BuildStep {
    Boolean getIsTestTask()
    Iterable<String> getNames()
}