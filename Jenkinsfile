pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    environment {
        DOCKER_REGISTRY = "agenciatelabranca"
        APP_NAME = "bibliotecadigital"
        BACKEND_IMAGE = "${DOCKER_REGISTRY}/${APP_NAME}-backend"
        FRONTEND_IMAGE = "${DOCKER_REGISTRY}/${APP_NAME}-frontend"
        PROJECT_DIR = "/home/user/projects/software-historia"
    }

    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                sshagent(credentials: ['github-ssh-key']) {
                    sh 'GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=accept-new" git clone --branch main --single-branch git@github.com:lmateusfaria/software-historia.git .'
                }
            }
        }

        stage('Prepare Env') {
            steps {
                sh 'if [ -f "$PROJECT_DIR/.env" ]; then cp "$PROJECT_DIR/.env" .env; fi'
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER'),
                    string(credentialsId: 'openai-api-key', variable: 'OPENAI_API_KEY')
                ]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    sh 'export OPENAI_API_KEY; TAG="$BUILD_NUMBER"; docker build -t "$BACKEND_IMAGE:$TAG" -t "$BACKEND_IMAGE:latest" ./backend; docker build -t "$FRONTEND_IMAGE:$TAG" -t "$FRONTEND_IMAGE:latest" ./front; docker push "$BACKEND_IMAGE:$TAG"; docker push "$BACKEND_IMAGE:latest"; docker push "$FRONTEND_IMAGE:$TAG"; docker push "$FRONTEND_IMAGE:latest"'
                }
            }
        }

        stage('Deploy Production') {
            steps {
                withCredentials([
                    string(credentialsId: 'openai-api-key', variable: 'OPENAI_API_KEY')
                ]) {
                    sh 'export OPENAI_API_KEY; docker rm -f bibliotecadigitalunifef-back bibliotecadigitalunifef-front bibliotecadigitalunifef-tunnel >/dev/null 2>&1 || true; docker compose -f docker-compose.yml pull; docker compose -f docker-compose.yml --profile tunnel up -d --remove-orphans; docker ps --filter "name=bibliotecadigitalunifef" --format "{{.Names}} {{.Status}}"'
                }
            }
        }

        stage('Healthcheck') {
            steps {
                sh 'sleep 30; STATUS=$(docker inspect -f "{{.State.Health.Status}}" bibliotecadigitalunifef-back 2>/dev/null || echo "unknown"); echo "Backend status: $STATUS"; if [ "$STATUS" != "healthy" ]; then docker logs --tail 80 bibliotecadigitalunifef-back || true; exit 1; fi'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
