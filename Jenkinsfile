pipeline {
    /*https://www.jenkins.io/doc/book/pipeline/jenkinsfile/*/
    agent any

    triggers {
        githubPush()
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