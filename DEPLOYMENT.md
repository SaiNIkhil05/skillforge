# SkillForge Deployment Guide

This guide details how to deploy SkillForge into production using free cloud tiers or Docker containerization.

---

## Architecture Overview
- **Frontend:** React 19 + Vite (Static Single Page App with Editorial UI)
- **Backend:** Java 17 + Spring Boot 4 (REST APIs, Apache Tika, JWT Security)
- **Database:** MySQL 8.0+

---

## Option 1: 100% Free Cloud Deployment (Recommended)

This setup uses **Vercel** (Frontend) + **Render** (Backend) + **Aiven or Clever Cloud** (MySQL).

### Step 1: Set up a Free Cloud MySQL Database
Choose one of the following free MySQL providers:
1. **Aiven (Free Tier)**:
   - Go to [aiven.io](https://aiven.io) and create a free MySQL service.
   - Note your `Host`, `Port`, `User`, `Password`, and `Database Name` (`defaultdb` or create `skillforge2_db`).
2. **Clever Cloud (Free Add-on)**:
   - Go to [clever-cloud.com](https://www.clever-cloud.com), create a free MySQL add-on, and copy the JDBC connection URL.
3. **Railway**:
   - Create a new project, add **MySQL Database**, and copy `MYSQL_URL`.

---

### Step 2: Deploy Spring Boot Backend to Render
1. Sign up / log in to [render.com](https://render.com).
2. Click **New +** ➔ **Web Service**.
3. Connect your GitHub repository: `https://github.com/SaiNIkhil05/skillforge`.
4. Configure service settings:
   - **Name:** `skillforge-backend`
   - **Region:** Nearest to your users (e.g., Singapore / Frankfurt / Oregon)
   - **Branch:** `main`
   - **Root Directory:** *(leave blank)*
   - **Runtime:** `Docker` (Render will automatically detect `Dockerfile`)
     *(Or Java: Build command `./mvnw clean package -DskipTests`, Start command `java -jar target/skillforge-0.0.1-SNAPSHOT.jar`)*
   - **Instance Type:** `Free`
5. Under **Environment Variables**, add:
   | Key | Value |
   | --- | --- |
   | `SPRING_DATASOURCE_URL` | `jdbc:mysql://<HOST>:<PORT>/<DATABASE>?useSSL=true&requireSSL=false` |
   | `SPRING_DATASOURCE_USERNAME` | Your database username |
   | `SPRING_DATASOURCE_PASSWORD` | Your database password |
   | `GEMINI_API_KEY` | *(Optional)* Your Google Gemini API Key |
6. Click **Create Web Service**.
7. Once deployed, copy your live backend URL (e.g. `https://skillforge-backend.onrender.com`).

---

### Step 3: Deploy Frontend to Vercel
1. Sign up / log in to [vercel.com](https://vercel.com).
2. Click **Add New...** ➔ **Project**.
3. Import your GitHub repository: `https://github.com/SaiNIkhil05/skillforge`.
4. In the project configuration:
   - **Framework Preset:** `Vite`
   - **Root Directory:** Click `Edit` and select `frontend`
5. Expand **Environment Variables** and add:
   | Key | Value |
   | --- | --- |
   | `VITE_API_URL` | Your Render backend URL with `/api` (e.g. `https://skillforge-backend.onrender.com/api`) |
6. Click **Deploy**.
7. In ~60 seconds, your site is live at `https://skillforge-xxx.vercel.app`!

---

## Option 2: 1-Command Local or VPS Deployment (Docker Compose)

If you have a Linux server (Ubuntu/Debian on AWS EC2, DigitalOcean, Hetzner, etc.) or want to run everything locally in containers:

### Prerequisites
- Docker Engine & Docker Compose installed.

### Launch:
```bash
# Clone the repository
git clone https://github.com/SaiNIkhil05/skillforge.git
cd skillforge

# Launch MySQL, Spring Boot backend, and Nginx-powered React frontend
docker compose up --build -d
```

### Access:
- **Frontend:** `http://localhost` (or your VPS public IP)
- **Backend API:** `http://localhost:8080`
- **Default Seed Accounts:**
  - **Student:** `student@college.edu` / `Student@123`
  - **Admin:** `admin@college.edu` / `Admin@123`

---

## Option 3: All-in-One Deployment on Railway

1. Sign up at [railway.app](https://railway.app).
2. Click **New Project** ➔ **Provision MySQL**.
3. In the same project, click **New** ➔ **GitHub Repo** ➔ select `skillforge`.
4. Under Settings, set `Root Directory` to `/` for backend. Set environment variables linking to the MySQL instance (`${{MySQL.MYSQL_URL}}`).
5. Add another service from the same repo for `frontend` and set `VITE_API_URL`.

---

## Production Verification Checklist
- [x] CORS origins allow `*` and `*.vercel.app` with credentials.
- [x] Client-side routing rewrite rules configured in `frontend/vercel.json` and `frontend/public/_redirects`.
- [x] Dynamic backend URL injection supported via `VITE_API_URL`.
- [x] Multi-stage Dockerfiles optimized for minimal footprint and non-root execution.
- [x] Port dynamically adapts to cloud provider `${PORT}` variable.
