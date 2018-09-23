node {

    stage('Gradle Build') {

        if (isUnix()) {

            sh './gradlew clean build -x test'

        } else {

            bat 'gradlew.bat clean build -x test'

        }

    }

}