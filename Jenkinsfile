pipeline {
    agent any
    
    options {
        // This enables GitHub integration for status checks
        githubProjectProperty(projectUrlStr: 'https://github.com/jpnino/ecommerce-microservice-backend-app')
    }
    
    triggers {
        // This sets up GitHub webhook trigger
        githubPush()
    }
      
    environment {
        // Azure credentials and configs
        AZURE_CREDS = credentials('azure-credentials')
        AKS_CLUSTER_NAME = 'ecommerce-aks'
        AKS_RESOURCE_GROUP = 'ecommerce-rg'
        
        // DockerHub credentials
        DOCKER_CREDS = credentials('docker-credentials')
        DOCKER_USERNAME = 'jpnino'
        
        // Application versioning
        APP_VERSION = "${BUILD_NUMBER}"
        GIT_BRANCH = "${env.GIT_BRANCH}"
        
        // Test configurations
        NEWMAN_VERSION = '5.3.2'
        COLLECTION_PATH = './e2e-tests/newman/collections/ecommerce-e2e.collection.json'
        ENVIRONMENT_PATH = './e2e-tests/newman/environments/environment.json'
        
        // Add GitHub credentials
        GITHUB_CREDS = credentials('github-credentials')
    }
    
    stages {
        stage('Checkout') {
            steps {
                // Clean workspace before checking out
                cleanWs()
                // Checkout code from GitHub
                checkout scm
            }
        }

        stage('Determine Environment') {
            steps {
                script {                    env.DEPLOY_ENV = ''
                    env.SHOULD_DEPLOY = false
                    
                    // Simple branch to environment mapping
                    switch(env.BRANCH_NAME) {
                        case 'dev':
                            env.DEPLOY_ENV = 'dev'
                            env.SHOULD_DEPLOY = true
                            break
                        case 'stage':
                            env.DEPLOY_ENV = 'stage'
                            env.SHOULD_DEPLOY = true
                            break
                        case 'main':
                            env.DEPLOY_ENV = 'prod'
                            env.SHOULD_DEPLOY = true
                            break
                        default:
                            echo "Branch ${env.BRANCH_NAME} is not configured for deployment"
                            currentBuild.result = 'NOT_BUILT'
                            return
                    }
                    
                    echo "Detected environment: ${env.DEPLOY_ENV}"
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                script {
                    githubNotify(context: 'Unit Tests', description: 'Running unit tests...', status: 'PENDING')
                      // For now, only run tests for product-service and user-service
                    def services = ['product-service', 'user-service']
                    
                    for (service in services) {
                        dir(service) {
                            sh './mvnw clean test'
                            // Archive test results immediately
                            junit "**/target/surefire-reports/*.xml"
                        }
                    }
                }
            }
            post {
                success {
                    githubNotify(context: 'Unit Tests', description: 'Unit tests passed', status: 'SUCCESS')
                }
                failure {
                    githubNotify(context: 'Unit Tests', description: 'Unit tests failed', status: 'FAILURE')
                    error 'Unit tests failed'
                }
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }        stage('Integration Tests') {
            steps {
                script {
                    githubNotify(context: 'Integration Tests', description: 'Running integration tests...', status: 'PENDING')
                    
                    // For now, only run tests for product-service and user-service
                    def services = ['product-service', 'user-service']
                    
                    for (service in services) {
                        dir(service) {
                            sh './mvnw verify -DskipUnitTests'
                        }
                    }
                }
            }
            post {
                success {
                    githubNotify(context: 'Integration Tests', description: 'Integration tests passed', status: 'SUCCESS')
                }
                failure {
                    githubNotify(context: 'Integration Tests', description: 'Integration tests failed', status: 'FAILURE')
                    error 'Integration tests failed'
                }
                always {
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }
        
        stage('Build & Push Docker Images') {
            when {
                expression { env.SHOULD_DEPLOY }
            }
            steps {
                script {                    def services = ['user-service', 'order-service', 'product-service', 
                                  'payment-service', 'shipping-service', 'favourite-service']
                    
                    // Login to DockerHub
                    sh "echo \$DOCKER_CREDS_PSW | docker login -u \$DOCKER_CREDS_USR --password-stdin"
                    
                    for (service in services) {
                        dir(service) {
                            def imageTag = "${DOCKER_USERNAME}/${service}:${env.DEPLOY_ENV}-${APP_VERSION}"
                            def latestTag = "${DOCKER_USERNAME}/${service}:latest"
                            
                            sh "docker build -t ${imageTag} -t ${latestTag} ."
                            sh "docker push ${imageTag}"
                            
                            // Only update latest tag for prod deployments
                            if (env.DEPLOY_ENV == 'prod') {
                                sh "docker push ${latestTag}"
                            }
                              // Update k8s manifests with new image tag using sed
                            sh """
                                # Update the image tag in the manifest
                                sed -i "s|${DOCKER_USERNAME}/${service}:.*|${DOCKER_USERNAME}/${service}:${env.DEPLOY_ENV}-${APP_VERSION}|g" k8s/${env.DEPLOY_ENV}/${service}-deployment.yml
                                
                                # Configure Git for commits
                                git config user.email "jenkins@example.com"
                                git config user.name "Jenkins CI"
                                
                                # Stage and commit the changes
                                git add k8s/${env.DEPLOY_ENV}/${service}-deployment.yml
                                git commit -m "Update ${service} image to ${env.DEPLOY_ENV}-${APP_VERSION} [skip ci]"
                                
                                # Push changes back to the repository
                                git push origin ${env.BRANCH_NAME}
                            """
                        }
                    }
                }
            }
        }
        
        stage('Deploy to AKS') {
            when {
                expression { env.SHOULD_DEPLOY }
            }
            steps {
                script {
                    githubNotify(context: 'Deployment', description: 'Deploying to AKS...', status: 'PENDING')
                    // Connect to AKS
                    sh """
                        az aks get-credentials \
                            --resource-group ${AKS_RESOURCE_GROUP} \
                            --name ${AKS_CLUSTER_NAME} \
                            --overwrite-existing
                    """
                      // Apply environment-specific configurations with namespace
                    // Create or ensure namespace exists
                    sh "kubectl create namespace ${env.DEPLOY_ENV} --dry-run=client -o yaml | kubectl apply -f -"
                    
                    // Step 1: Apply core infrastructure services
                    echo "Deploying core infrastructure services..."
                    dir('k8s/core') {
                        sh "kubectl apply -f . -n ${env.DEPLOY_ENV}"
                        
                        // Wait for core services to be ready
                        sh "kubectl rollout status deployment/cloud-config -n ${env.DEPLOY_ENV} --timeout=300s"
                        sh "kubectl rollout status deployment/zipkin -n ${env.DEPLOY_ENV} --timeout=300s"
                    }
                    
                    // Step 2: Deploy service discovery
                    echo "Deploying service discovery..."
                    dir("k8s/${env.DEPLOY_ENV}") {
                        sh "kubectl apply -f service-discovery-deployment.yml -n ${env.DEPLOY_ENV}"
                        sh "kubectl rollout status deployment/service-discovery -n ${env.DEPLOY_ENV} --timeout=300s"
                    }
                    
                    // Step 3: Wait for service discovery to be fully ready (additional buffer)
                    sh "sleep 30"
                    
                    // Step 4: Deploy API Gateway
                    echo "Deploying API Gateway..."
                    dir("k8s/${env.DEPLOY_ENV}") {
                        sh "kubectl apply -f api-gateway-deployment.yml -n ${env.DEPLOY_ENV}"
                        sh "kubectl rollout status deployment/api-gateway -n ${env.DEPLOY_ENV} --timeout=300s"
                    }
                    
                    // Step 5: Deploy all other microservices
                    echo "Deploying microservices..."
                    dir("k8s/${env.DEPLOY_ENV}") {
                        def baseServices = ['user-service', 'product-service']
                        def dependentServices = ['order-service', 'payment-service', 
                                               'shipping-service', 'favourite-service']
                        
                        // Deploy base services first
                        for (service in baseServices) {
                            sh "kubectl apply -f ${service}-deployment.yml -n ${env.DEPLOY_ENV}"
                            sh "kubectl rollout status deployment/${service} -n ${env.DEPLOY_ENV} --timeout=300s"
                        }
                        
                        // Then deploy services with dependencies
                        for (service in dependentServices) {
                            sh "kubectl apply -f ${service}-deployment.yml -n ${env.DEPLOY_ENV}"
                            sh "kubectl rollout status deployment/${service} -n ${env.DEPLOY_ENV} --timeout=300s"
                        }
                    }
                    }
                }
            }
            post {
                success {
                    githubNotify(context: 'Deployment', description: 'Deployment successful', status: 'SUCCESS')
                }
                failure {
                    githubNotify(context: 'Deployment', description: 'Deployment failed', status: 'FAILURE')
                }
            }
        }          stage('E2E Tests') {
            when {
                expression { env.BRANCH_NAME == 'stage' || env.BRANCH_NAME == 'main' }
            }
            steps {
                script {
                    githubNotify(context: 'E2E Tests', description: 'Running E2E tests...', status: 'PENDING')
                    
                    try {
                        // Build Newman Docker image
                        dir('e2e-tests/newman') {
                            def newmanImage = "${DOCKER_USERNAME}/newman:${env.DEPLOY_ENV}-${APP_VERSION}"
                            sh "docker build -t ${newmanImage} ."
                        }
                          // Run tests using docker with environment-specific config
                        def envFile
                        switch(env.BRANCH_NAME) {
                            case 'main':
                                envFile = 'aks-prod'
                                break
                            case 'stage':
                                envFile = 'aks-stage'
                                break
                            default:
                                error "E2E tests not configured for branch ${env.BRANCH_NAME}"
                        }
                        
                        sh """
                            docker run --network=host \
                            -v "\${WORKSPACE}/e2e-tests/newman/collections:/etc/newman/collections" \
                            -v "\${WORKSPACE}/e2e-tests/newman/environments:/etc/newman/environments" \
                            -v "\${WORKSPACE}/e2e-tests/newman/reports:/etc/newman/reports" \
                            ${newmanImage} run collections/ecommerce-e2e.collection.json \
                            --environment environments/${envFile}.environment.json \
                            --reporters cli,junit,htmlextra \
                            --reporter-junit-export reports/junit-report.xml \
                            --reporter-htmlextra-export reports/newman-report.html
                        """
                        
                        // Publish test results
                        junit 'e2e-tests/newman/reports/junit-report.xml'
                        
                        // Archive HTML report
                        archiveArtifacts artifacts: 'e2e-tests/newman/reports/newman-report.html', 
                                       allowEmptyArchive: true
                    } catch (Exception e) {
                        githubNotify(context: 'E2E Tests', description: 'E2E tests failed', status: 'FAILURE')
                        error "E2E tests failed: ${e.message}"
                    }
                    
                    githubNotify(context: 'E2E Tests', description: 'E2E tests passed', status: 'SUCCESS')
                }
            }
        }
        
        stage('Generate Release Notes') {
            when {
                expression { env.DEPLOY_ENV == 'prod' }
            }
            steps {
                script {
                    def changelog = sh(
                        script: 'git log $(git describe --tags --abbrev=0)..HEAD --pretty=format:"%h - %s"',
                        returnStdout: true
                    )
                    
                    writeFile file: 'release-notes.md', text: """
                        # Release Notes - Version ${APP_VERSION}
                        
                        ## Changes
                        ${changelog}
                        
                        ## Deployment Info
                        - Environment: Production
                        - Build Number: ${BUILD_NUMBER}
                        - Deployment Date: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
                    """
                    
                    archiveArtifacts artifacts: 'release-notes.md'
                }
            }
        }
    }
    
    post {
        always {
            // Archive test reports
            archiveArtifacts artifacts: 'e2e-tests/reports/**/*', allowEmptyArchive: true
            
            // Send notifications
            script {
                def envName = env.DEPLOY_ENV ?: 'unknown'
                def status = currentBuild.result ?: 'SUCCESS'
                
                emailext subject: "${envName.toUpperCase()} Build ${status}: ${currentBuild.fullDisplayName}",
                         body: """
                            Build: ${currentBuild.fullDisplayName}
                            Status: ${status}
                            Environment: ${envName}
                            Changes: ${currentBuild.changeSets.size() > 0 ? currentBuild.changeSets.collect { it.items }.flatten().collect { "${it.author.fullName}: ${it.msg}" }.join('\n') : 'No changes'}
                            Build URL: ${env.BUILD_URL}
                         """,
                         to: '${DEFAULT_RECIPIENTS}'
            }
        }
    }
}
