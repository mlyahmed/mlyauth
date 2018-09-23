node {
    def server = Artifactory.newServer url: SERVER_URL, credentialsId: CREDENTIALS
    def rtGradle = Artifactory.newGradleBuild()
    def buildInfo = Artifactory.newBuildInfo()


    stage('Clone the branch'){
        checkout scm
    }

    stage('Build') {
        sh './gradlew clean install -x test'
    }

    stage('Test') {
        echo 'Testing..'
    }

    stage('Deploy') {
        echo 'Deploying....'
    }

}