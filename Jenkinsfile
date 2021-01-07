pipeline {
    /*https://www.jenkins.io/doc/book/pipeline/jenkinsfile/*/
    agent any

    triggers {
        pollSCM('*/10 * * * *') //polling for changes, here once a minute
    }

    environment {
        ORGANIZATION_NAME = "w-k-s"
        PROJECT_NAME = "completable-futures-experiment"
    }

    stages {
            stage('Clone') {
                steps {
                    checkout scm
                }
            }
            stage('Test and Build') {
                steps {
                    sh './gradlew clean build'
                }
            }
    }
}