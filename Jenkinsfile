pipeline {

    agent any

    environment {

        PATH = "/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.PATH}"

        DOCKER_USERNAME = "swapnamotupally"

        EUREKA_IMAGE    = "swapnamotupally/eureka-server"
        EMPLOYEE_IMAGE  = "swapnamotupally/employee-service"
        DEPARTMENT_IMAGE= "swapnamotupally/department-service"
        GATEWAY_IMAGE   = "swapnamotupally/api-gateway"

        IMAGE_TAG = "${BUILD_NUMBER}"

        AWS_REGION = "ap-south-1"

        EKS_CLUSTER = "microservices-cluster"

        K8S_NAMESPACE = "microservices"
    }

    stages {

    }

    post {

    }

}