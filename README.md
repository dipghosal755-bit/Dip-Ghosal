# AI SOS Guardian 🛡️

**AI SOS Guardian** is an AI-assisted emergency SOS, live-location tracking, risk analysis engine, and responder command center designed for student projects, Smart India Hackathon (SIH), and real-world personal safety applications.

---

## 🌟 Key Features

### 1. User Mobile App (Android - Jetpack Compose & Kotlin)
- **Large Circular SOS Button:** Central high-visibility, pulsing beacon button with tactile vibration and one-tap emergency activation.
- **Real-Time GPS Tracking:** Live location streaming at configurable intervals (e.g., 10 seconds), with tactical satellite radar sweep and visual breadcrumb path.
- **Safety Zones & Geofencing:** Designated zones (`College Campus`, `Hostel Block`, `Home`). Detects route deviations (+10 risk points).
- **Automatic Fall & Anomaly Detection:** Built-in accelerometer listener detecting sudden high-G impact spikes, followed by an alert countdown dialog (*"Possible emergency detected. Are you safe?"*) before automated distress escalation.
- **Emergency Contacts:** Store guardians (Mother, Father, Friend, Campus Security) with instant single-tap phone dialer and SMS broadcast draft.
- **Local Prototype Mode & Remote FastAPI Sync:** Runs 100% self-contained on-device using Android Room database, with seamless HTTP synchronization to Python FastAPI backend when configured.

### 2. AI Risk Scoring Engine
Calculates multi-signal distress priority based on real-time telemetry:
- **Manual SOS:** `+50 pts`
- **Sudden Fall Detected:** `+25 pts`
- **Prolonged Inactivity:** `+15 pts`
- **Route Deviation / Safe Zone Exit:** `+10 pts`
- **Late Night Isolation:** `+5 pts`
- **Low Battery (<15%):** `+5 pts`

#### Priority Levels:
- `0 – 19`: **LOW** (🟢)
- `20 – 39`: **MEDIUM** (🟡)
- `40 – 69`: **HIGH** (🟠)
- `70 – 100`: **CRITICAL** (🔴)

*Disclaimer: AI-assisted prototype risk model evaluating compounded telemetry signals; designed for prototype and hackathon demonstration.*

### 3. Responder Command Center
- Multi-incident triage queue sorted strictly by priority (`CRITICAL` → `HIGH` → `MEDIUM` → `LOW`).
- Interactive Live Tactical Map with user epicenter, safety zone radius, moving markers, and breadcrumbs.
- Responder Unit Dispatch action (`"Dispatch Patrol"`) and emergency resolution (`"Resolve"`).
- Available both inside the Android app and via the Python FastAPI web dashboard (`http://localhost:8000/dashboard`).

---

## 🏗️ System Architecture
```
Android Mobile App (Kotlin + Compose)
       │  ▲
       │  │ (GPS, Fall Sensors, Distress, Breadcrumbs)
       ▼  │
Room Local DB / Offline Engine  ◄──►  FastAPI Backend (Python)
                                              │
                                              ├─ Risk Analysis Engine
                                              ├─ Live Location Breadcrumbs
                                              └─ Web Command Center (Leaflet.js)
```

---

## 🚀 How to Run

### Mobile Application:
1. Open the project in Android Studio or run via the emulator preview.
2. The app compiles with modern Kotlin and Jetpack Compose.
3. Test GPS simulation, fall simulation, and route deviation directly using the demonstration chips on the home screen!

### Python FastAPI Backend:
1. Navigate to `/backend`:
   ```bash
   cd backend
   pip install -r requirements.txt
   uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
   ```
2. Open Swagger Docs: `http://localhost:8000/docs`
3. Open Web Dashboard: `http://localhost:8000/dashboard`
