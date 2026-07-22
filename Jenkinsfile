pipeline {
    agent any

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