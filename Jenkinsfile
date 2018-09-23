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
                sh './gradlew clean install -x test'
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