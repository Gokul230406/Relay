# StreakSaver 🛡️🔥

> **Personal Coding Streak Protection System for LeetCode, CodeChef, and GeeksforGeeks.**

StreakSaver ensures developers never accidentally lose their hard-earned coding streaks on busy days, while strictly guaranteeing ethical platform compliance and a core safety invariant:

```
FOR EACH USER AND EACH CALENDAR DAY:
MAXIMUM SUBMISSION ATTEMPTS = 1
```

---

## 🌟 Key Features

- **Multi-Platform Monitoring**: Tracks submission activity across **LeetCode**, **CodeChef**, and **GeeksforGeeks** every calendar day.
- **Strict 1-Submission Invariant Guard**: Uses Redis distributed locking, MongoDB compound unique constraints (`(userId, date)`), and a double-check pattern to enforce that **at most 1 submission attempt** can occur per calendar day, even under concurrent API triggers, server restarts, or scheduler retries.
- **Configurable Priority Selection**: Automatically selects the highest-priority missing platform according to user preferences (e.g. `1. LeetCode`, `2. CodeChef`, `3. GeeksforGeeks`).
- **Safe Pre-Approved Problem Pool**: Only submits solutions explicitly provided by the user in their personal Problem Pool. Never generates arbitrary or unapproved code.
- **Timezone-Aware Emergency Scheduler**: Daily emergency run configured for a specific time (e.g., 11:30 PM) in the user's local timezone (default: `Asia/Kolkata`).
- **Developer-Focused Dashboard**: Modern Dark UI with 🔥 streak counters, live platform status badges, emergency countdown, calendar history, and drag-and-drop priority ordering.

---

## 🏗️ Architecture Overview

```
                      ┌─────────────────────────────────────────┐
                      │          Next.js Frontend (TS)          │
                      │  Dashboard | History | Pool | Settings  │
                      └────────────────────┬────────────────────┘
                                           │ REST API (JWT)
                                           ▼
                      ┌─────────────────────────────────────────┐
                      │       Spring Boot Backend Engine        │
                      └────┬───────────────────────────────┬────┘
                           │                               │
                           ▼                               ▼
      ┌─────────────────────────┐                     ┌─────────────────────────┐
      │   Redis Distributed Lock│                     │   MongoDB Persistence   │
      │   lock:daily_sub:{user} │                     │ unique(userId, date)    │
      └─────────────────────────┘                     └─────────────────────────┘
```

### Safety Guard Execution Flow

```
1. TRIGGER (Scheduler / Manual "Check Now")
         │
         ▼
2. CHECK DAILY GUARD IN MONGODB (If present → RETURN LIMIT_REACHED)
         │
         ▼
3. ACQUIRE REDIS DISTRIBUTED LOCK (TTL: 30s)
         │
         ▼
4. RE-CHECK DAILY GUARD (Double-check after lock)
         │
         ▼
5. CHECK ALL 3 PLATFORMS (LeetCode, CodeChef, GFG)
         │
         ▼
6. EVALUATE DECISION (All submitted? → Save NO_ACTION, Exit)
         │
         ▼
7. SELECT HIGHEST-PRIORITY MISSING PLATFORM
         │
         ▼
8. SAVE PENDING GUARD RECORD IN MONGODB (Unique (userId, date) Index)
         │
         ▼
9. EXECUTE PERMITTED SUBMISSION FROM PROBLEM POOL
         │
         ▼
10. STORE RESULT (SUCCESS / FAILED) & RELEASE REDIS LOCK
```

---

## 🛠️ Tech Stack

- **Frontend**: Next.js 14/15, React, TypeScript, Tailwind CSS
- **Backend**: Spring Boot 3.4+, Java 21, Spring Security (JWT), Spring Data MongoDB, Spring Data Redis, Spring Scheduler
- **Database**: MongoDB 7.0 (with unique compound index on `(userId, date)`)
- **Cache & Locks**: Redis 7.2 (Distributed locking & idempotency protection)
- **Containerization**: Docker & Docker Compose

---

## 🚀 Quickstart & Setup Guide

### Option 1: Docker Compose (Recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/streaksaver.git
   cd streaksaver
   ```

2. Launch all services (MongoDB, Redis, Backend, Frontend):
   ```bash
   docker-compose up --build
   ```

3. Access the dashboard:
   - Frontend UI: `http://localhost:3000`
   - Backend API: `http://localhost:8080/api`

---

### Option 2: Local Development Setup

#### Prerequisites
- Java 21+
- Node.js 20+ & npm
- MongoDB running locally on port `27017`
- Redis running locally on port `6379`

#### Backend Setup
```bash
cd backend
./gradlew bootRun
```

#### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

---

## 🔑 Environment Variables

### Backend (`application.yml` / ENV)
| Variable | Default Value | Description |
|---|---|---|
| `SPRING_DATA_MONGODB_URI` | `mongodb://localhost:27017/streaksaver` | MongoDB Connection URI |
| `SPRING_REDIS_HOST` | `localhost` | Redis Server Host |
| `SPRING_REDIS_PORT` | `6379` | Redis Server Port |
| `JWT_SECRET` | `streaksaver_secret_key_...` | JWT Secret Key |

### Frontend (`.env.local`)
| Variable | Default Value | Description |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080/api` | Backend API Base URL |

---

## 🧪 Comprehensive Testing & Concurrency Verification

Run unit tests and the critical **10-thread concurrency integration test**:

```bash
cd backend
./gradlew test
```

### Critical Concurrency Test Scenario
`StreakSaverConcurrencyTest.java` fires **10 simultaneous threads** attempting emergency submission for the exact same user and date:
- **Expected Outcome**: Exactly **1 submission attempt** is executed; the remaining 9 concurrent calls are immediately blocked by Redis distributed locking and MongoDB's `(userId, date)` unique constraint.

---

## 📌 API Endpoints Reference

### Authentication
- `POST /api/auth/register` – Register new user account
- `POST /api/auth/login` – Login and acquire JWT
- `POST /api/auth/logout` – Invalidate active session

### Dashboard & Streaks
- `GET /api/dashboard` – Fetch aggregated streak counts, platform status, and emergency scheduler state
- `POST /api/streak/check` – Query live platform statuses without attempting submission
- `POST /api/streak/emergency-submit` – Trigger manual check & emergency submission (strictly obeys daily guard!)

### Platform Connections & Settings
- `GET /api/platforms/status` – View platform connection handles
- `POST /api/platforms/{platform}/connect` – Connect platform username handle
- `GET /api/settings` – Retrieve platform priority, emergency time, timezone
- `PUT /api/settings` – Update priority order, emergency time, timezone, auto-submit toggles

### Problem Pool Management
- `GET /api/problem-pool` – Retrieve user-approved problems & solutions
- `POST /api/problem-pool/{platform}` – Add new approved problem/solution
- `DELETE /api/problem-pool/{platform}/{id}` – Remove problem from pool

---

## ⚖️ Security, Compliance & Platform Terms

StreakSaver adheres strictly to platform security guidelines:
- **No Anti-Bot/CAPTCHA Bypassing**: StreakSaver does **not** contain stealth automation, CAPTCHA bypass mechanisms, or credential theft vectors.
- **Permitted Integration**: Integrates via official APIs or permitted user endpoints.
- **User-Approved Problem Pool**: StreakSaver never submits arbitrary code or randomly generated submissions.
- **Token Security**: Platform tokens and passwords are stored encrypted and are **never** logged or returned in API responses.

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for details.
