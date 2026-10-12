# ==========================================
# SkillForge Frontend Production Dockerfile
# Multi-stage build with Nginx Alpine
# ==========================================

# Stage 1: Build Vite Bundle
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve with lightweight Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html

# SPA Routing & API Reverse Proxy Nginx Configuration
RUN echo 'server {' \
    '    listen 80;' \
    '    server_name localhost;' \
    '    location / {' \
    '        root /usr/share/nginx/html;' \
    '        index index.html index.htm;' \
    '        try_files $uri $uri/ /index.html;' \
    '    }' \
    '    location /api/ {' \
    '        proxy_pass http://backend:8080/api/;' \
    '        proxy_set_header Host $host;' \
    '        proxy_set_header X-Real-IP $remote_addr;' \
    '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' \
    '    }' \
    '}' > /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
