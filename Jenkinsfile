pipeline {
    agent any
    def server = Artifactory.newServer url: SERVER_URL, credentialsId: CREDENTIALS
    def rtGradle = Artifactory.newGradleBuild()
    def buildInfo = Artifactory.newBuildInfo()

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