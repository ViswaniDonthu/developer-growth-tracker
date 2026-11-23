# Developer Growth Tracker (GrowthOS)

A personal 8-month study tracker built with **React** + **Spring Boot 3** + **PostgreSQL** bundled into a single unified Docker container.

## Plan Dates
- **Start**: 2026-11-12
- **End**: 2027-07-11
- **Daily schedule entries**: 242
- **Timezone for calculations**: `Asia/Kolkata`

---

## Deploying on Render (Single Web Service with Docker + Neon DB)

You only need to deploy **ONE Web Service** on Render. The root `Dockerfile` automatically builds the React frontend and embeds it inside the Spring Boot `.jar`.

### 1. Set Up Neon Database
Your Neon connection string is ready:
- **`DATABASE_URL`**: `jdbc:postgresql://ep-long-bonus-bd41m4l3-pooler.c-8.us-east-2.aws.neon.tech:5432/neondb?sslmode=require`
- **`DB_USERNAME`**: `neondb_owner`
- **`DB_PASSWORD`**: `npg_7H2LJiIcCYGR`

### 2. Deploy to Render (1-Click or Manual)
1. Push this repository to GitHub or GitLab.
2. In Render ([dashboard.render.com](https://dashboard.render.com)):
   - **Method A (Blueprint)**: Click **New +** > **Blueprint**, connect your repo. Render reads `render.yaml` and sets up the `growth-tracker` service.
   - **Method B (Web Service)**: Click **New +** > **Web Service**, select your repo, choose **Docker** runtime.
3. In the service **Environment** settings, add:
   - `DATABASE_URL`: `jdbc:postgresql://ep-long-bonus-bd41m4l3-pooler.c-8.us-east-2.aws.neon.tech:5432/neondb?sslmode=require`
   - `DB_USERNAME`: `neondb_owner`
   - `DB_PASSWORD`: `npg_7H2LJiIcCYGR`
   - `PORT`: `8080`
4. Click **Deploy**.

Render will build both Frontend and Backend, start the app on port 8080, and connect to Neon.
Access your app at `https://<your-service-name>.onrender.com`.

---

## Run Locally with Docker Compose

```bash
docker compose up --build
```
Open `http://localhost:8080` in your browser.
