pipeline {
    agent any

    environment {
        PATH = "/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.PATH}"
    }

    stages {
        stage('Verify Tools') {
            steps {
                sh 'git --version'
                sh 'mvn --version'
                sh 'docker --version'
                sh 'aws --version'
                sh 'kubectl version --client'
                sh 'eksctl version'
                sh 'kubectl get nodes'
            }
        }
    }
}