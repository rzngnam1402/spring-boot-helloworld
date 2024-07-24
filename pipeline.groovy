pipeline {
  agent any

  environment {
    SECOND_INSTANCE_IP = '3.82.15.132'
    REPO_URL = 'https://github.com/rzngnam1402/spring-boot-helloworld'
    SECOND_INSTANCE_SSH_KEY = '5efcbd92-9bc8-4508-9625-2901432c2c0a'
    APP_NAME = 'spring-boot-helloworld'
    JAR_NAME = 'hello-world-0.0.1-SNAPSHOT.jar'
  }

  stages {
    stage('Clone Repository') {
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: SECOND_INSTANCE_SSH_KEY, keyFileVariable: 'SSH_KEY_FILE')]) {
          script {
            sh """
            ssh -i \${SSH_KEY_FILE} ubuntu@\${SECOND_INSTANCE_IP} " \
              if [ ! -d \"/home/ubuntu/${APP_NAME}\" ]; then \
                git clone ${REPO_URL} /home/ubuntu/${APP_NAME}; \
              else \
                cd /home/ubuntu/${APP_NAME} && git fetch && git checkout main && git pull origin main; \
              fi"
            """
          }
        }
      }
    }

    stage('Build Application') {
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: SECOND_INSTANCE_SSH_KEY, keyFileVariable: 'SSH_KEY_FILE')]) {
          script {
            sh """
            ssh -i \${SSH_KEY_FILE} ubuntu@\${SECOND_INSTANCE_IP} " \
              cd /home/ubuntu/${APP_NAME} \
              && mvn clean package -X"
            """
          }
        }
      }
    }

    stage('Deploy Application') {
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: SECOND_INSTANCE_SSH_KEY, keyFileVariable: 'SSH_KEY_FILE')]) {
          script {
            sh """
            ssh -i \${SSH_KEY_FILE} ubuntu@\${SECOND_INSTANCE_IP} "

              nohup java -jar /home/ubuntu/${APP_NAME}/target/${JAR_NAME} > /home/ubuntu/app.log 2>&1 &

            "
            """
          }
        }
      }
    }
  }

  post {
    success {
      echo 'Build and deployment succeeded!'
    }
    failure {
      echo 'Build and deployment failed!'
    }
  }
}
