pipeline {

    agent any

    environment {
        DOCKERHUB_USERNAME = 'swapnamotupally'
        DOCKERHUB_CREDENTIAL_ID = 'dockerhub-credentials'

        IMAGE_TAG = "${BUILD_NUMBER}"
        K8S_NAMESPACE = 'microservices'

        EUREKA_IMAGE =
                "${DOCKERHUB_USERNAME}/eureka-server:${IMAGE_TAG}"

        EMPLOYEE_IMAGE =
                "${DOCKERHUB_USERNAME}/employee-service:${IMAGE_TAG}"

        DEPARTMENT_IMAGE =
                "${DOCKERHUB_USERNAME}/department-service:${IMAGE_TAG}"

        GATEWAY_IMAGE =
                "${DOCKERHUB_USERNAME}/api-gateway:${IMAGE_TAG}"
    }

    stages {

        stage('Create Application Artifacts') {
            parallel {

                stage('Build Eureka Server') {
                    steps {
                        dir('EurekaServer') {
                            sh '''
                                mvn clean package -DskipTests
                                ls -l target
                            '''
                        }
                    }
                }

                stage('Build Employee Service') {
                    steps {
                        dir('employee-service') {
                            sh '''
                                mvn clean package -DskipTests
                                ls -l target
                            '''
                        }
                    }
                }

                stage('Build Department Service') {
                    steps {
                        dir('department-service') {
                            sh '''
                                mvn clean package -DskipTests
                                ls -l target
                            '''
                        }
                    }
                }

                stage('Build API Gateway') {
                    steps {
                        dir('ApiGateway') {
                            sh '''
                                mvn clean package -DskipTests
                                ls -l target
                            '''
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh '''
                    docker build \
                    -t ${EUREKA_IMAGE} \
                    ./EurekaServer

                    docker build \
                    -t ${EMPLOYEE_IMAGE} \
                    ./employee-service

                    docker build \
                    -t ${DEPARTMENT_IMAGE} \
                    ./department-service

                    docker build \
                    -t ${GATEWAY_IMAGE} \
                    ./ApiGateway
                '''
            }
        }

        stage('Docker Hub Login') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: "${DOCKERHUB_CREDENTIAL_ID}",
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" |
                        docker login \
                        -u "$DOCKER_USERNAME" \
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

        stage('Verify Kubernetes Connection') {
            steps {
                sh '''
                    kubectl cluster-info
                    kubectl get nodes
                '''
            }
        }

        stage('Create Kubernetes Namespace') {
            steps {
                sh '''
                    kubectl apply \
                    -f kubernetes/namespace.yml
                '''
            }
        }

        stage('Apply Kubernetes Manifests') {
            steps {
                sh '''
                    kubectl apply \
                    -f kubernetes/ \
                    -n ${K8S_NAMESPACE}
                '''
            }
        }

        stage('Deploy Images to EKS') {
            steps {
                sh '''
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

        stage('Verify Rolling Deployment') {
            steps {
                sh '''
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

        stage('Display Deployment Status') {
            steps {
                sh '''
                    echo "Deployments:"
                    kubectl get deployments \
                    -n ${K8S_NAMESPACE}

                    echo "Pods:"
                    kubectl get pods \
                    -n ${K8S_NAMESPACE}

                    echo "Services:"
                    kubectl get services \
                    -n ${K8S_NAMESPACE}
                '''
            }
        }
    }

    post {

        success {
            echo """
            CI/CD pipeline completed successfully.

            Docker image tag: ${IMAGE_TAG}
            Kubernetes namespace: ${K8S_NAMESPACE}
            """
        }

        failure {
            echo 'Pipeline failed. Collecting Kubernetes information.'

            sh '''
                kubectl get pods \
                -n ${K8S_NAMESPACE} || true

                kubectl get events \
                -n ${K8S_NAMESPACE} \
                --sort-by=.metadata.creationTimestamp || true
            '''
        }

        always {
            sh '''
                docker logout || true
            '''
        }
    }
}