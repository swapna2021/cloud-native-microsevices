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

        // Jenkins-specific kubeconfig.
        // This prevents corruption of ~/.kube/config.
        KUBECONFIG = "${WORKSPACE}/.kube/config"
    }

    options {
        disableConcurrentBuilds()
        timestamps()

        buildDiscarder(
            logRotator(
                numToKeepStr: '10'
            )
        )

        timeout(
            time: 45,
            unit: 'MINUTES'
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
                    set -e

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

        stage('Validate Kubernetes Files') {
            steps {
                sh '''
                    set -e

                    echo "Checking Kubernetes manifest files..."

                    test -f kubernetes/namespace.yml
                    test -f kubernetes/gp3-storageclass.yml
                    test -f kubernetes/mysql.yml
                    test -f kubernetes/eureka.yml
                    test -f kubernetes/employee.yml
                    test -f kubernetes/department.yml
                    test -f kubernetes/api-gateway.yml

                    echo "All required Kubernetes files are available."
                '''
            }
        }

        stage('Build Maven Projects') {
            parallel {

                stage('Build Eureka Server') {
                    steps {
                        dir('EurekaServer') {
                            sh '''
                                set -e

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
                                set -e

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
                                set -e

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
                                set -e

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
                    set -e

                    echo "Building Eureka Server image..."

                    docker build \
                        --platform linux/amd64 \
                        -t "${EUREKA_IMAGE}" \
                        ./EurekaServer

                    echo "Building Employee Service image..."

                    docker build \
                        --platform linux/amd64 \
                        -t "${EMPLOYEE_IMAGE}" \
                        ./employee-service

                    echo "Building Department Service image..."

                    docker build \
                        --platform linux/amd64 \
                        -t "${DEPARTMENT_IMAGE}" \
                        ./department-service

                    echo "Building API Gateway image..."

                    docker build \
                        --platform linux/amd64 \
                        -t "${GATEWAY_IMAGE}" \
                        ./ApiGateway

                    echo "Created Docker images:"

                    docker images |
                        grep "${DOCKERHUB_USERNAME}" || true
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
                        set -e

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
                    set -e

                    echo "Pushing Eureka image..."
                    docker push "${EUREKA_IMAGE}"

                    echo "Pushing Employee Service image..."
                    docker push "${EMPLOYEE_IMAGE}"

                    echo "Pushing Department Service image..."
                    docker push "${DEPARTMENT_IMAGE}"

                    echo "Pushing API Gateway image..."
                    docker push "${GATEWAY_IMAGE}"
                '''
            }
        }

        stage('Configure Amazon EKS') {
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
                        set -e

                        echo "AWS identity:"
                        aws sts get-caller-identity

                        echo "Creating Jenkins-specific kubeconfig..."

                        mkdir -p "${WORKSPACE}/.kube"

                        # Remove any old or corrupted workspace kubeconfig.
                        rm -f "${KUBECONFIG}"

                        aws eks update-kubeconfig \
                            --region "${AWS_REGION}" \
                            --name "${EKS_CLUSTER}" \
                            --kubeconfig "${KUBECONFIG}"

                        echo "Current Kubernetes context:"
                        kubectl config current-context

                        echo "Cluster nodes:"
                        kubectl get nodes -o wide
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
                        set -e

                        kubectl apply \
                            -f kubernetes/namespace.yml
                    '''
                }
            }
        }

        stage('Configure EBS Storage') {
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
                        set -e

                        echo "Applying gp3 StorageClass..."

                        kubectl apply \
                            -f kubernetes/gp3-storageclass.yml

                        echo "Available StorageClasses:"

                        kubectl get storageclass
                    '''
                }
            }
        }

        stage('Deploy MySQL') {
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
                        set -e

                        echo "Applying MySQL resources..."

                        kubectl apply \
                            -f kubernetes/mysql.yml \
                            -n "${K8S_NAMESPACE}"

                        echo "Waiting for MySQL PVC to become Bound..."

                        kubectl wait \
                            --for=jsonpath='{.status.phase}'=Bound \
                            pvc/mysql-pvc \
                            -n "${K8S_NAMESPACE}" \
                            --timeout=300s

                        echo "MySQL PVC status:"

                        kubectl get pvc \
                            -n "${K8S_NAMESPACE}"

                        echo "Waiting for MySQL Deployment..."

                        kubectl rollout status \
                            deployment/mysql-db \
                            -n "${K8S_NAMESPACE}" \
                            --timeout=300s
                    '''
                }
            }
        }

        stage('Deploy Eureka Server') {
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
                        set -e

                        echo "Deploying Eureka Server..."

                        kubectl apply \
                            -f kubernetes/eureka.yml \
                            -n "${K8S_NAMESPACE}"

                        kubectl set image \
                            deployment/eureka-server \
                            eureka-server="${EUREKA_IMAGE}" \
                            -n "${K8S_NAMESPACE}"

                        kubectl patch deployment eureka-server \
                            -n "${K8S_NAMESPACE}" \
                            --type='strategic' \
                            -p='{
                                "spec": {
                                    "template": {
                                        "spec": {
                                            "containers": [
                                                {
                                                    "name": "eureka-server",
                                                    "imagePullPolicy": "Always"
                                                }
                                            ]
                                        }
                                    }
                                }
                            }'

                        kubectl rollout status \
                            deployment/eureka-server \
                            -n "${K8S_NAMESPACE}" \
                            --timeout=300s
                    '''
                }
            }
        }

        stage('Deploy Business Services') {
            parallel {

                stage('Deploy Employee Service') {
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
                                set -e

                                echo "Deploying Employee Service..."

                                kubectl apply \
                                    -f kubernetes/employee.yml \
                                    -n "${K8S_NAMESPACE}"

                                kubectl set image \
                                    deployment/employee-service \
                                    employee-service="${EMPLOYEE_IMAGE}" \
                                    -n "${K8S_NAMESPACE}"

                                kubectl patch deployment employee-service \
                                    -n "${K8S_NAMESPACE}" \
                                    --type='strategic' \
                                    -p='{
                                        "spec": {
                                            "template": {
                                                "spec": {
                                                    "containers": [
                                                        {
                                                            "name": "employee-service",
                                                            "imagePullPolicy": "Always"
                                                        }
                                                    ]
                                                }
                                            }
                                        }
                                    }'

                                kubectl rollout status \
                                    deployment/employee-service \
                                    -n "${K8S_NAMESPACE}" \
                                    --timeout=300s
                            '''
                        }
                    }
                }

                stage('Deploy Department Service') {
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
                                set -e

                                echo "Deploying Department Service..."

                                kubectl apply \
                                    -f kubernetes/department.yml \
                                    -n "${K8S_NAMESPACE}"

                                kubectl set image \
                                    deployment/department-service \
                                    department-service="${DEPARTMENT_IMAGE}" \
                                    -n "${K8S_NAMESPACE}"

                                kubectl patch deployment department-service \
                                    -n "${K8S_NAMESPACE}" \
                                    --type='strategic' \
                                    -p='{
                                        "spec": {
                                            "template": {
                                                "spec": {
                                                    "containers": [
                                                        {
                                                            "name": "department-service",
                                                            "imagePullPolicy": "Always"
                                                        }
                                                    ]
                                                }
                                            }
                                        }
                                    }'

                                kubectl rollout status \
                                    deployment/department-service \
                                    -n "${K8S_NAMESPACE}" \
                                    --timeout=300s
                            '''
                        }
                    }
                }
            }
        }

        stage('Deploy API Gateway') {
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
                        set -e

                        echo "Deploying API Gateway..."

                        kubectl apply \
                            -f kubernetes/api-gateway.yml \
                            -n "${K8S_NAMESPACE}"

                        kubectl set image \
                            deployment/api-gateway \
                            api-gateway="${GATEWAY_IMAGE}" \
                            -n "${K8S_NAMESPACE}"

                        kubectl patch deployment api-gateway \
                            -n "${K8S_NAMESPACE}" \
                            --type='strategic' \
                            -p='{
                                "spec": {
                                    "template": {
                                        "spec": {
                                            "containers": [
                                                {
                                                    "name": "api-gateway",
                                                    "imagePullPolicy": "Always"
                                                }
                                            ]
                                        }
                                    }
                                }
                            }'

                        kubectl rollout status \
                            deployment/api-gateway \
                            -n "${K8S_NAMESPACE}" \
                            --timeout=300s
                    '''
                }
            }
        }

        stage('Verify Complete Deployment') {
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
                        set -e

                        echo "Waiting for all deployments to become available..."

                        kubectl wait \
                            --for=condition=Available \
                            deployment/mysql-db \
                            deployment/eureka-server \
                            deployment/employee-service \
                            deployment/department-service \
                            deployment/api-gateway \
                            -n "${K8S_NAMESPACE}" \
                            --timeout=300s

                        echo "Confirming deployed image tags..."

                        kubectl get deployments \
                            -n "${K8S_NAMESPACE}" \
                            -o 'custom-columns=DEPLOYMENT:.metadata.name,IMAGE:.spec.template.spec.containers[*].image'
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
                        set -e

                        echo "=============================="
                        echo "Nodes"
                        echo "=============================="

                        kubectl get nodes \
                            -o 'custom-columns=NAME:.metadata.name,TYPE:.metadata.labels.node\\.kubernetes\\.io/instance-type,POD-CAPACITY:.status.capacity.pods'

                        echo "=============================="
                        echo "Deployments"
                        echo "=============================="

                        kubectl get deployments \
                            -n "${K8S_NAMESPACE}"

                        echo "=============================="
                        echo "Deployed Images"
                        echo "=============================="

                        kubectl get deployments \
                            -n "${K8S_NAMESPACE}" \
                            -o 'custom-columns=DEPLOYMENT:.metadata.name,IMAGE:.spec.template.spec.containers[*].image'

                        echo "=============================="
                        echo "Pods"
                        echo "=============================="

                        kubectl get pods \
                            -n "${K8S_NAMESPACE}" \
                            -o wide

                        echo "=============================="
                        echo "Services"
                        echo "=============================="

                        kubectl get services \
                            -n "${K8S_NAMESPACE}"

                        echo "=============================="
                        echo "PersistentVolumeClaims"
                        echo "=============================="

                        kubectl get pvc \
                            -n "${K8S_NAMESPACE}"

                        echo "=============================="
                        echo "StorageClasses"
                        echo "=============================="

                        kubectl get storageclass

                        echo "=============================="
                        echo "Jenkins Build"
                        echo "=============================="

                        echo "Build number: ${BUILD_NUMBER}"
                        echo "Docker image tag: ${IMAGE_TAG}"
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
                    set +e

                    mkdir -p "${WORKSPACE}/.kube"

                    # Recreate the workspace kubeconfig safely.
                    rm -f "${KUBECONFIG}"

                    aws eks update-kubeconfig \
                        --region "${AWS_REGION}" \
                        --name "${EKS_CLUSTER}" \
                        --kubeconfig "${KUBECONFIG}"

                    echo "=============================="
                    echo "Nodes"
                    echo "=============================="

                    kubectl get nodes -o wide || true

                    echo "=============================="
                    echo "Deployments"
                    echo "=============================="

                    kubectl get deployments \
                        -n "${K8S_NAMESPACE}" || true

                    echo "=============================="
                    echo "Deployed Images"
                    echo "=============================="

                    kubectl get deployments \
                        -n "${K8S_NAMESPACE}" \
                        -o 'custom-columns=DEPLOYMENT:.metadata.name,IMAGE:.spec.template.spec.containers[*].image' || true

                    echo "=============================="
                    echo "Pods"
                    echo "=============================="

                    kubectl get pods \
                        -n "${K8S_NAMESPACE}" \
                        -o wide || true

                    echo "=============================="
                    echo "PersistentVolumeClaims"
                    echo "=============================="

                    kubectl get pvc \
                        -n "${K8S_NAMESPACE}" || true

                    echo "=============================="
                    echo "StorageClasses"
                    echo "=============================="

                    kubectl get storageclass || true

                    echo "=============================="
                    echo "Pod descriptions"
                    echo "=============================="

                    kubectl describe pods \
                        -n "${K8S_NAMESPACE}" || true

                    echo "=============================="
                    echo "Recent events"
                    echo "=============================="

                    kubectl get events \
                        -n "${K8S_NAMESPACE}" \
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