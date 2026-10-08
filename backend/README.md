# AI SOS Guardian - FastAPI Backend & Command Center

AI-assisted emergency SOS, live-location tracking, risk analysis engine, and responder dashboard. Designed for Smart India Hackathon & academic demonstrations (BCA / B.Tech).

---

## 🛠️ Tech Stack
- **Framework:** Python FastAPI
- **Server:** Uvicorn (ASGI)
- **Validation:** Pydantic v2
- **Risk Analysis:** Scikit-Learn synthetic ensemble + heuristic rule weights
- **Mapping:** OpenStreetMap + Leaflet.js
- **Database:** In-memory reactive store (extensible to SQLite/PostgreSQL/Firestore)

---

## 🚀 Quick Setup & Run Instructions

### 1. Create Virtual Environment
```bash
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

### 2. Install Dependencies
```bash
pip install -r requirements.txt
```

### 3. Launch the Backend Server
```bash
uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
```

- **Interactive API Swagger Docs:** `http://localhost:8000/docs`
- **Responder Command Center Web Dashboard:** `http://localhost:8000/dashboard`

---

## 📡 API Endpoints Summary

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/sos` | Triggers SOS, calculates AI risk score, starts tracking |
| `POST` | `/location` | Streams GPS coordinates during active emergency |
| `GET` | `/emergencies` | Fetches emergencies sorted by priority (`CRITICAL` → `LOW`) |
| `GET` | `/locations/{user_id}` | Retrieves historical GPS breadcrumb path |
| `POST` | `/emergency/{id}/cancel` | Cancels emergency distress signal |
| `POST` | `/emergency/{id}/resolve`| Resolves incident from responder dashboard |
| `GET` | `/health` | Health check endpoint |

---

## 🧪 Run Automated Tests
```bash
pytest backend/test_api.py -v
```

---

## 💡 AI Risk Calculation Weights
- **Manual SOS:** `+50 pts`
- **Fall Detected:** `+25 pts`
- **Inactivity / No Movement:** `+15 pts`
- **Route Deviation:** `+10 pts`

### Risk Levels:
- **0 – 19:** `LOW` (🟢)
- **20 – 39:** `MEDIUM` (🟡)
- **40 – 69:** `HIGH` (🟠)
- **70 – 100:** `CRITICAL` (🔴)
