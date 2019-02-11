node {
    // Get Artifactory server instance, defined in the Artifactory Plugin administration page.
    def server = Artifactory.server "SERVER_ID"
    // Create an Artifactory Gradle instance.
    def rtGradle = Artifactory.newGradleBuild()
    def buildInfo

    stage('Clone sources') {
        checkout scm
    }

    stage('Artifactory configuration') {
        rtGradle.tool = "Gradle-2.4"
        rtGradle.deployer repo:'ext-release-local', server: server
        rtGradle.resolver repo:'remote-repos', server: server
    }

    stage('Gradle build') {
        buildInfo = rtGradle.run rootDir: ".", buildFile: 'build.gradle', tasks: 'clean artifactoryPublish'
    }

    stage('Publish build info') {
        server.publishBuildInfo buildInfo
    }
}