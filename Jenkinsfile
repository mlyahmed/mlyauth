pipeline {
    agent any

    stages {

        stage('Clone the branch'){
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                if (isUnix()) {

                    sh './gradlew clean build -x test'

                } else {

                    bat 'gradlew.bat clean build -x test'

                }
            }
        }

        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }

    }
}