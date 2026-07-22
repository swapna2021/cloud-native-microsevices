pipeline {

    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    environment {

        // Homebrew tools used by Jenkins on macOS
        PATH = "/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.PATH}"

        DOCKERHUB_USERNAME = "swapnamotupally"
        DOCKER_CREDENTIALS_ID = "dockerhub-credentials"
        AWS_CREDENTIALS_ID = "aws-credentials"

        IMAGE_TAG = "${BUILD_NUMBER}"

        EUREKA_IMAGE =
                "${DOCKERHUB_USERNAME}/eureka-server:${IMAGE_TAG}"

        EMPLOYEE_IMAGE =
                "${DOCKERHUB_USERNAME}/employee-service:${IMAGE_TAG}"

        DEPARTMENT_IMAGE =
                "${DOCKERHUB_USERNAME}/department-service:${IMAGE_TAG}"

        GATEWAY_IMAGE =
                "${DOCKERHUB_USERNAME}/api-gateway:${IMAGE_TAG}"

        AWS_REGION = "ap-south-1"
        EKS_CLUSTER = "microservices-cluster"
        K8S_NAMESPACE = "microservices"
    }

    options {
        disableConcurrentBuilds()
        timestamps()

        buildDiscarder(
            logRotator(
                numToKeepStr: '10'
            )
        )
    }

    stages {

        stage('Checkout Source Code') {
            steps {
                checkout scm
            }
        }

        stage('Verify Required Tools') {
            steps {
                sh '''
                    echo "Git:"
                    git --version

                    echo "Java:"
                    java -version

                    echo "Maven:"
                    mvn --version

                    echo "Docker:"
                    docker --version

                    echo "AWS CLI:"
                    aws --version

                    echo "kubectl:"
                    kubectl version --client
                '''
            }
        }

        stage('Build Maven Projects') {
            parallel {

                stage('Build Eureka Server') {
                    steps {
                        dir('EurekaServer') {
                            sh '''
                                mvn clean package -DskipTests
                                ls -la target
                            '''
                        }
                    }
                }

                stage('Build Employee Service') {
                    steps {
                        dir('employee-service') {
                            sh '''
                                mvn clean package -DskipTests
                                ls -la target
                            '''
                        }
                    }
                }

                stage('Build Department Service') {
                    steps {
                        dir('department-service') {
                            sh '''
                                mvn clean package -DskipTests
                                ls -la target
                            '''
                        }
                    }
                }

                stage('Build API Gateway') {
                    steps {
                        dir('ApiGateway') {
                            sh '''
                                mvn clean package -DskipTests
                                ls -la target
                            '''
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh '''
                    echo "Building Eureka Server image..."
                    docker build \
                        --platform linux/amd64 \
                        -t ${EUREKA_IMAGE} \
                        ./EurekaServer

                    echo "Building Employee Service image..."
                    docker build \
                        --platform linux/amd64 \
                        -t ${EMPLOYEE_IMAGE} \
                        ./employee-service

                    echo "Building Department Service image..."
                    docker build \
                        --platform linux/amd64 \
                        -t ${DEPARTMENT_IMAGE} \
                        ./department-service

                    echo "Building API Gateway image..."
                    docker build \
                        --platform linux/amd64 \
                        -t ${GATEWAY_IMAGE} \
                        ./ApiGateway

                    echo "Created Docker images:"
                    docker images | grep swapnamotupally
                '''
            }
        }

        stage('Login to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_TOKEN'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_TOKEN" |
                        docker login \
                            --username "$DOCKER_USERNAME" \
                            --password-stdin
                    '''
                }
            }
        }

        stage('Push Images to Docker Hub') {
            steps {
                sh '''
                    docker push ${EUREKA_IMAGE}
                    docker push ${EMPLOYEE_IMAGE}
                    docker push ${DEPARTMENT_IMAGE}
                    docker push ${GATEWAY_IMAGE}
                '''
            }
        }

        stage('Connect Jenkins to Amazon EKS') {
            steps {
                withCredentials([
                    [
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: "${AWS_CREDENTIALS_ID}",
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]
                ]) {
                    sh '''
                        aws sts get-caller-identity

                        aws eks update-kubeconfig \
                            --region ${AWS_REGION} \
                            --name ${EKS_CLUSTER}

                        kubectl config current-context
                        kubectl get nodes
                    '''
                }
            }
        }

        stage('Create Kubernetes Namespace') {
            steps {
                withCredentials([
                    [
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: "${AWS_CREDENTIALS_ID}",
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]
                ]) {
                    sh '''
                        aws eks update-kubeconfig \
                            --region ${AWS_REGION} \
                            --name ${EKS_CLUSTER}

                        kubectl apply \
                            -f kubernetes/namespace.yml
                    '''
                }
            }
        }

        stage('Deploy Kubernetes Resources') {
            steps {
                withCredentials([
                    [
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: "${AWS_CREDENTIALS_ID}",
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]
                ]) {
                    sh '''
                        aws eks update-kubeconfig \
                            --region ${AWS_REGION} \
                            --name ${EKS_CLUSTER}

                        kubectl apply \
                            -f kubernetes/mysql.yml \
                            -n ${K8S_NAMESPACE}

                        kubectl apply \
                            -f kubernetes/eureka.yml \
                            -n ${K8S_NAMESPACE}

                        kubectl apply \
                            -f kubernetes/employee.yml \
                            -n ${K8S_NAMESPACE}

                        kubectl apply \
                            -f kubernetes/department.yml \
                            -n ${K8S_NAMESPACE}

                        kubectl apply \
                            -f kubernetes/api-gateway.yml \
                            -n ${K8S_NAMESPACE}
                    '''
                }
            }
        }

        stage('Update Kubernetes Images') {
            steps {
                withCredentials([
                    [
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: "${AWS_CREDENTIALS_ID}",
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]
                ]) {
                    sh '''
                        aws eks update-kubeconfig \
                            --region ${AWS_REGION} \
                            --name ${EKS_CLUSTER}

                        kubectl set image \
                            deployment/eureka-server \
                            eureka-server=${EUREKA_IMAGE} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image \
                            deployment/employee-service \
                            employee-service=${EMPLOYEE_IMAGE} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image \
                            deployment/department-service \
                            department-service=${DEPARTMENT_IMAGE} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image \
                            deployment/api-gateway \
                            api-gateway=${GATEWAY_IMAGE} \
                            -n ${K8S_NAMESPACE}
                    '''
                }
            }
        }

        stage('Verify Kubernetes Rollout') {
            steps {
                withCredentials([
                    [
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: "${AWS_CREDENTIALS_ID}",
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]
                ]) {
                    sh '''
                        aws eks update-kubeconfig \
                            --region ${AWS_REGION} \
                            --name ${EKS_CLUSTER}

                        kubectl rollout status \
                            deployment/eureka-server \
                            -n ${K8S_NAMESPACE} \
                            --timeout=300s

                        kubectl rollout status \
                            deployment/employee-service \
                            -n ${K8S_NAMESPACE} \
                            --timeout=300s

                        kubectl rollout status \
                            deployment/department-service \
                            -n ${K8S_NAMESPACE} \
                            --timeout=300s

                        kubectl rollout status \
                            deployment/api-gateway \
                            -n ${K8S_NAMESPACE} \
                            --timeout=300s
                    '''
                }
            }
        }

        stage('Display Deployment Status') {
            steps {
                withCredentials([
                    [
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: "${AWS_CREDENTIALS_ID}",
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]
                ]) {
                    sh '''
                        aws eks update-kubeconfig \
                            --region ${AWS_REGION} \
                            --name ${EKS_CLUSTER}

                        echo "Deployments:"
                        kubectl get deployments \
                            -n ${K8S_NAMESPACE}

                        echo "Pods:"
                        kubectl get pods \
                            -n ${K8S_NAMESPACE} \
                            -o wide

                        echo "Services:"
                        kubectl get services \
                            -n ${K8S_NAMESPACE}

                        echo "Docker image tag deployed:"
                        echo "${IMAGE_TAG}"
                    '''
                }
            }
        }
    }

    post {

        success {
            echo """
            CI/CD pipeline completed successfully.

            Jenkins build: ${BUILD_NUMBER}
            Docker image tag: ${IMAGE_TAG}
            EKS cluster: ${EKS_CLUSTER}
            Kubernetes namespace: ${K8S_NAMESPACE}
            """
        }

        failure {
            echo 'Pipeline failed. Collecting diagnostic information.'

            withCredentials([
                [
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: "${AWS_CREDENTIALS_ID}",
                    accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                    secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                ]
            ]) {
                sh '''
                    aws eks update-kubeconfig \
                        --region ${AWS_REGION} \
                        --name ${EKS_CLUSTER} || true

                    echo "Pods:"
                    kubectl get pods \
                        -n ${K8S_NAMESPACE} \
                        -o wide || true

                    echo "Deployments:"
                    kubectl get deployments \
                        -n ${K8S_NAMESPACE} || true

                    echo "Recent events:"
                    kubectl get events \
                        -n ${K8S_NAMESPACE} \
                        --sort-by=.metadata.creationTimestamp || true
                '''
            }
        }

        always {
            sh '''
                docker logout || true
            '''
        }
    }
}