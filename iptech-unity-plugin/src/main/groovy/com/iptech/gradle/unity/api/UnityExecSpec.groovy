package com.iptech.gradle.unity.api

interface UnityExecSpec {
    void arguments(List<String> arguments)
    void projectPath(String projectPath)
    void buildTarget(String buildTarget)
    void ignoreExitValue(Boolean ignoreExitValue)
    void userName(String userName)
    void password(String password)
    void logFile(String logFile)
    void unityCmdPath(Object unityCmdPath)
    void environment(Map<String,String> environment)
    void environment(String key, String value)
}
