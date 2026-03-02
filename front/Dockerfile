# Estágio de Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

# Estágio de Execução com Nginx
FROM nginx:alpine
# O caminho de saída do Angular 18 costuma ser dist/[nome-do-projeto]/browser
COPY --from=build /app/dist/frontEndAngular/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
