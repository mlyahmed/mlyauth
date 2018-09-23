pipeline {
    agent any
    def rtGradle = Artifactory.newGradleBuild()
    def buildInfo = Artifactory.newBuildInfo()

    stages {
       stage ('Configuration') {
           rtGradle.tool = GRADLE_TOOL // Tool name from Jenkins configuration
       }

        stage('Clone the branch'){
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building..'
                rtGradle.run rootDir: ".", buildFile: 'build.gradle', tasks: 'clean build -x test', buildInfo: buildInfo
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