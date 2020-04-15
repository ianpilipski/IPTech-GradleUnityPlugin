package com.iptech.gradle.unity.internal

import com.iptech.gradle.unity.api.UnityExecSpec
import groovy.transform.CompileStatic

@CompileStatic
class DefaultUnityExecSpec implements UnityExecSpec {
    List<String> arguments
    String projectPath
    String buildTarget
    Boolean ignoreExitValue = false
    String userName
    String password
    String logFile
    Object unityCmdPath
    Map<String,String> environmentVars

    DefaultUnityExecSpec() {
        environmentVars = ((Map<String,String>)new HashMap()) << System.getenv()
    }

    @Override
    void arguments(List<String> arguments) {
        this.arguments = arguments
    }

    @Override
    void projectPath(String projectPath) {
        this.projectPath = projectPath
    }

    @Override
    void buildTarget(String buildTarget) {
        this.buildTarget = buildTarget
    }

    @Override
    void ignoreExitValue(Boolean ignoreExitValue) {
        this.ignoreExitValue = ignoreExitValue
    }

    @Override
    void userName(String userName) {
        this.userName = userName
    }

    @Override
    void password(String password) {
        this.password = password
    }

    @Override
    void logFile(String logFile) {
        this.logFile = logFile
    }

    @Override
    void unityCmdPath(Object unityCmdPath) {
        this.unityCmdPath = unityCmdPath
    }

    @Override
    void environment(Map<String,String> env) {
        this.environmentVars = env
    }

    @Override
    void environment(String key, String value) {
        this.environmentVars[key] = value
    }
}
