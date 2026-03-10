pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "agenciatelabranca"
        APP_NAME = "bibliotecadigital"
        BACKEND_IMAGE = "${DOCKER_REGISTRY}/${APP_NAME}-backend"
        FRONTEND_IMAGE = "${DOCKER_REGISTRY}/${APP_NAME}-frontend"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                    script {
                        def tag = env.BUILD_NUMBER
                        
                        // Login no Docker Hub
                        sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"
                        
                        // Build das Imagens
                        sh "docker build --no-cache -t ${BACKEND_IMAGE}:${tag} -t ${BACKEND_IMAGE}:latest ./backend"
                        sh "docker build --no-cache -t ${FRONTEND_IMAGE}:${tag} -t ${FRONTEND_IMAGE}:latest ./front"
                        
                        // Push das Imagens
                        sh "docker push ${BACKEND_IMAGE}:${tag}"
                        sh "docker push ${BACKEND_IMAGE}:latest"
                        sh "docker push ${FRONTEND_IMAGE}:${tag}"
                        sh "docker push ${FRONTEND_IMAGE}:latest"
                    }
                }
            }
        }

        stage('Deploy Production') {
            steps {
                echo 'Realizando deploy no ambiente de Produção...'
                script {
                    // Tenta copiar o .env de locais prováveis, mas garante a execução mesmo se falhar
                    sh '''
                        if [ -f /home/user/bibliotecadigitalunifef/.env ]; then
                            cp /home/user/bibliotecadigitalunifef/.env .env
                        elif [ -f /home/user/pdv/bibliotecadigital/.env ]; then
                            cp /home/user/pdv/bibliotecadigital/.env .env
                        elif [ -f /home/user/.env ]; then
                            cp /home/user/.env .env
                        else
                            echo "AVISO: .env não encontrado em locais padrão. Usando configurações internas."
                            touch .env
                        fi
                    '''
                    
                    sh "docker compose -p bibliotecadigital pull"
                    sh "docker compose -p bibliotecadigital up -d --remove-orphans"
                    
                    echo 'Aguardando inicialização dos serviços...'
                    sh 'sleep 20'
                    
                    // Verifica logs se o backend não estiver saudável
                    sh '''
                        STATUS=$(docker inspect -f '{{.State.Health.Status}}' bibliotecadigitalunifef-back || echo "unknown")
                        if [ "$STATUS" != "healthy" ]; then
                            echo "ERRO: Backend não está saudável. Verificando logs..."
                            docker logs --tail 50 bibliotecadigitalunifef-back
                        fi
                    '''
                    
                    // Limpa imagens antigas (dangling) para economizar recursos
                    sh "docker image prune -af || true"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline finalizado com sucesso!'
        }
        failure {
            echo 'Ocorreu um erro no pipeline. Verifique os logs.'
        }
    }
}
