pipeline {
    agent any

    environment {
        GIT_REPO_URL = 'https://github.com/rzngnam1402/spring-boot-helloworld'
        GIT_BRANCH = 'main'
        WORKDIR = 'Documents/Fast Retailing/hello-world'
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    dir(env.WORKDIR) {
                        if (!fileExists('.git')) {
                            echo 'Cloning repository...'
                            git branch: env.GIT_BRANCH, url: env.GIT_REPO_URL
                        } else {
                            echo 'Repository exists. Checking for updates...'
                            sh 'git fetch origin'
                            def status = sh(script: 'git status -uno', returnStdout: true).trim()
                            if (status.contains('Your branch is behind')) {
                                echo 'Updating repository...'
                                sh 'git pull origin ${GIT_BRANCH}'
                            } else {
                                echo 'Repository is up-to-date.'
                            }
                        }
                        sh 'mvn clean package'

                    }
                }
            }
        }

        stage('Docker Environment Validation') {
            steps {
                script {
                    // Check Docker installation
                    sh 'which docker || { echo "Docker not found"; exit 1; }'
                    sh 'docker --version'
                    sh 'docker-compose --version'
                }
            }
        }

        stage('Build and Deploy') {
            steps {
                script {
                    dir(env.WORKDIR) {
                        sh 'docker-compose -f ${DOCKER_COMPOSE_FILE} down --volumes'
                        sh 'docker-compose -f ${DOCKER_COMPOSE_FILE} up --build -d'
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment completed successfully!'
        }

        failure {
            echo 'Deployment failed. Review logs for details.'
        }

    }
}
