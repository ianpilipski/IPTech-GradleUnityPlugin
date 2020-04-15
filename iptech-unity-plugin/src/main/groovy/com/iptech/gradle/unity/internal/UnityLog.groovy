package com.iptech.gradle.unity.internal

import groovy.transform.CompileStatic

@CompileStatic
class UnityLog {
    private File logFile

    UnityLog(File logFile) {
        this.logFile = logFile
    }

    String parseLogForBuildErrors() {
        Boolean parsingException = false
        return this.logFile.readLines().findAll {  line ->
            if(parsingException) {
                if( line ==~ /(?m)^.*?at.*/ ) return true
                parsingException = false
            }
            if (line ==~ /(?m)^ERROR:.*/) return true
            if (line ==~ /(?m)^.*?Exception:.*/) {
                parsingException = true
                return true
            }
        }.join('\n')
    }
}
