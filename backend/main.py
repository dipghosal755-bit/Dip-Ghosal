import uuid
import time
from typing import Dict, List
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from backend.models import (
    SosRequest,
    LocationUpdateRequest,
    EmergencyRecord,
    CancelResponse,
    ContactCreate,
)
from backend.risk_engine import risk_engine

app = FastAPI(
    title="AI SOS Guardian Backend API",
    description="Emergency SOS, Live Location Tracking & AI Risk Assessment Engine",
    version="1.0.0"
)

# Enable CORS for web dashboard & mobile access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# In-memory prototype databases
emergencies_db: Dict[str, EmergencyRecord] = {}
locations_db: Dict[str, List[LocationUpdateRequest]] = {}  # key: user_id
contacts_db: Dict[str, List[ContactCreate]] = {}

# Seed initial emergency contacts
seed_user = "USER-DEMO-88"
contacts_db[seed_user] = [
    ContactCreate(user_id=seed_user, name="Mother", phone="+1 (555) 019-2831", relationship="Mother"),
    ContactCreate(user_id=seed_user, name="Campus Safety Patrol", phone="+1 (555) 911-0022", relationship="Campus Security"),
]

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "AI SOS Guardian API",
        "timestamp": int(time.time() * 1000)
    }

@app.post("/sos", response_model=EmergencyRecord)
def trigger_sos(req: SosRequest):
    """
    1. Create an emergency record
    2. Calculate AI-assisted risk score
    3. Store record and initialize live location tracking
    """
    risk_score, risk_level, contributing = risk_engine.evaluate_risk(
        is_manual_sos=(req.emergency_type == "MANUAL_SOS"),
        is_fall_detected=req.is_fall_detected,
        is_no_movement=req.is_no_movement,
        is_route_deviation=req.is_route_deviation
    )

    emergency_id = "EMG-" + str(uuid.uuid4())[:8].upper()
    now = int(time.time() * 1000)

    record = EmergencyRecord(
        emergency_id=emergency_id,
        user_id=req.user_id,
        latitude=req.latitude,
        longitude=req.longitude,
        emergency_type=req.emergency_type,
        risk_score=risk_score,
        risk_level=risk_level,
        status="ACTIVE",
        contributing_factors=contributing,
        timestamp=now,
        last_updated=now
    )

    emergencies_db[emergency_id] = record

    # Initialize location breadcrumbs
    loc = LocationUpdateRequest(
        emergency_id=emergency_id,
        user_id=req.user_id,
        latitude=req.latitude,
        longitude=req.longitude,
        accuracy=4.5,
        timestamp=now
    )
    locations_db.setdefault(req.user_id, []).append(loc)

    return record

@app.post("/location")
def update_location(req: LocationUpdateRequest):
    """
    Updates user location during active emergency.
    Updates coordinates and last_updated on the emergency record.
    """
    locations_db.setdefault(req.user_id, []).append(req)

    if req.emergency_id in emergencies_db:
        emg = emergencies_db[req.emergency_id]
        if emg.status == "ACTIVE":
            emg.latitude = req.latitude
            emg.longitude = req.longitude
            emg.last_updated = int(time.time() * 1000)

    return {
        "status": "success",
        "message": "Location updated",
        "recorded_points": len(locations_db.get(req.user_id, []))
    }

@app.get("/emergencies", response_model=List[EmergencyRecord])
def list_emergencies():
    """
    Returns emergencies sorted by priority:
    CRITICAL -> HIGH -> MEDIUM -> LOW
    """
    priority_order = {"CRITICAL": 4, "HIGH": 3, "MEDIUM": 2, "LOW": 1}
    all_records = list(emergencies_db.values())

    return sorted(
        all_records,
        key=lambda e: (
            e.status == "ACTIVE",
            priority_order.get(e.risk_level, 0),
            e.risk_score,
            e.last_updated
        ),
        reverse=True
    )

@app.get("/locations/{user_id}", response_model=List[LocationUpdateRequest])
def get_user_locations(user_id: str):
    """
    Returns location history/breadcrumbs for a given user.
    """
    return locations_db.get(user_id, [])

@app.post("/emergency/{emergency_id}/cancel", response_model=CancelResponse)
def cancel_emergency(emergency_id: str):
    """
    Cancels an active SOS.
    """
    if emergency_id not in emergencies_db:
        raise HTTPException(status_code=404, detail="Emergency record not found")

    emg = emergencies_db[emergency_id]
    emg.status = "CANCELLED"
    emg.last_updated = int(time.time() * 1000)

    return CancelResponse(
        emergency_id=emergency_id,
        status="CANCELLED",
        message="Emergency SOS deactivated by user."
    )

@app.post("/emergency/{emergency_id}/resolve")
def resolve_emergency(emergency_id: str):
    """
    Marks an emergency as resolved by responders.
    """
    if emergency_id not in emergencies_db:
        raise HTTPException(status_code=404, detail="Emergency record not found")

    emg = emergencies_db[emergency_id]
    emg.status = "RESOLVED"
    emg.last_updated = int(time.time() * 1000)

    return {"status": "RESOLVED", "emergency_id": emergency_id}

@app.get("/contacts/{user_id}", response_model=List[ContactCreate])
def get_contacts(user_id: str):
    return contacts_db.get(user_id, [])

@app.post("/contacts", response_model=ContactCreate)
def add_contact(contact: ContactCreate):
    contacts_db.setdefault(contact.user_id, []).append(contact)
    return contact

@app.get("/", response_class=HTMLResponse)
@app.get("/dashboard", response_class=HTMLResponse)
def responder_dashboard():
    """
    Web-based AI SOS Command Center with OpenStreetMap Leaflet live tracking.
    """
    return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI SOS Command Center - Responder Dashboard</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
        :root {
            --bg-dark: #0D0F12;
            --surface: #16191E;
            --surface-variant: #20242B;
            --border: #2C323D;
            --text-primary: #F1F3F5;
            --text-secondary: #9AA0A6;
            --red: #D32F2F;
            --orange: #E65100;
            --yellow: #F57F17;
            --green: #2E7D32;
            --cyan: #00B0FF;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
        body { background: var(--bg-dark); color: var(--text-primary); display: flex; flex-direction: column; height: 100vh; }
        header { background: var(--surface); border-bottom: 1px solid var(--border); padding: 14px 24px; display: flex; justify-content: space-between; align-items: center; }
        .logo { font-size: 1.25rem; font-weight: 800; display: flex; align-items: center; gap: 10px; }
        .badge { padding: 4px 10px; border-radius: 6px; font-size: 0.75rem; font-weight: 700; text-transform: uppercase; }
        .badge-critical { background: var(--red); color: white; }
        .badge-high { background: var(--orange); color: white; }
        .badge-medium { background: var(--yellow); color: black; }
        .badge-low { background: var(--green); color: white; }
        .layout { display: flex; flex: 1; overflow: hidden; }
        .sidebar { width: 440px; background: var(--surface); border-right: 1px solid var(--border); display: flex; flex-direction: column; overflow-y: auto; }
        .map-container { flex: 1; height: 100%; position: relative; }
        #map { width: 100%; height: 100%; background: #0c1017; }
        .stats-bar { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; padding: 14px; border-bottom: 1px solid var(--border); }
        .stat-card { background: var(--surface-variant); padding: 10px; border-radius: 8px; text-align: center; }
        .stat-val { font-size: 1.4rem; font-weight: 800; }
        .stat-lbl { font-size: 0.7rem; color: var(--text-secondary); text-transform: uppercase; }
        .incident-list { padding: 14px; display: flex; flex-direction: column; gap: 10px; }
        .incident-card { background: var(--surface-variant); border: 1px solid var(--border); border-radius: 10px; padding: 14px; cursor: pointer; transition: 0.2s; }
        .incident-card:hover { border-color: var(--cyan); }
        .incident-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
        .incident-user { font-weight: 700; font-size: 0.95rem; }
        .incident-meta { font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 8px; }
        .incident-actions { display: flex; gap: 8px; margin-top: 10px; }
        button { background: var(--surface); color: var(--text-primary); border: 1px solid var(--border); padding: 6px 12px; border-radius: 6px; font-weight: 600; font-size: 0.75rem; cursor: pointer; }
        button:hover { background: var(--border); }
        .btn-resolve { background: var(--green); color: white; border: none; }
        .btn-dispatch { background: var(--cyan); color: black; border: none; }
    </style>
</head>
<body>
    <header>
        <div class="logo">
            <span>🚨</span>
            <span>AI SOS Command Center</span>
            <span class="badge badge-critical" id="live-indicator">● LIVE RADAR</span>
        </div>
        <div>
            <span style="font-size: 0.85rem; color: var(--text-secondary);">Responder Unit ID: DISPATCH-CENTRAL-01</span>
        </div>
    </header>

    <div class="layout">
        <div class="sidebar">
            <div class="stats-bar">
                <div class="stat-card">
                    <div class="stat-val" id="stat-active" style="color: var(--red);">0</div>
                    <div class="stat-lbl">Active</div>
                </div>
                <div class="stat-card">
                    <div class="stat-val" id="stat-critical" style="color: var(--red);">0</div>
                    <div class="stat-lbl">Critical</div>
                </div>
                <div class="stat-card">
                    <div class="stat-val" id="stat-high" style="color: var(--orange);">0</div>
                    <div class="stat-lbl">High</div>
                </div>
                <div class="stat-card">
                    <div class="stat-val" id="stat-total" style="color: var(--cyan);">0</div>
                    <div class="stat-lbl">Total</div>
                </div>
            </div>

            <div style="padding: 14px 14px 0 14px; font-size: 0.8rem; font-weight: 700; color: var(--text-secondary);">
                PRIORITIZED INCIDENTS (CRITICAL → LOW)
            </div>

            <div class="incident-list" id="incidents-container">
                <div style="text-align: center; color: var(--text-secondary); padding: 20px;">Scanning for active emergency distress pings...</div>
            </div>
        </div>

        <div class="map-container">
            <div id="map"></div>
        </div>
    </div>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
        const map = L.map('map').setView([28.6139, 77.2090], 15);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        // Safety zone circle
        L.circle([28.6139, 77.2090], {
            color: '#2E7D32',
            fillColor: '#2E7D32',
            fillOpacity: 0.15,
            radius: 600
        }).addTo(map).bindPopup("<b>College Campus (Main Block)</b><br>Designated Safety Perimeter");

        let markers = {};
        let polylines = {};

        async function fetchEmergencies() {
            try {
                const res = await fetch('/emergencies');
                const list = await res.json();
                renderDashboard(list);
            } catch (err) {
                console.error("Fetch failed", err);
            }
        }

        function renderDashboard(emergencies) {
            const container = document.getElementById('incidents-container');
            const active = emergencies.filter(e => e.status === 'ACTIVE');
            const critical = active.filter(e => e.risk_level === 'CRITICAL');
            const high = active.filter(e => e.risk_level === 'HIGH');

            document.getElementById('stat-active').textContent = active.length;
            document.getElementById('stat-critical').textContent = critical.length;
            document.getElementById('stat-high').textContent = high.length;
            document.getElementById('stat-total').textContent = emergencies.length;

            if (emergencies.length === 0) {
                container.innerHTML = '<div style="text-align:center;color:#9AA0A6;padding:20px;">No distress signals active. All sectors safe.</div>';
                return;
            }

            container.innerHTML = emergencies.map(emg => {
                const badgeClass = 'badge-' + emg.risk_level.toLowerCase();
                const isActive = emg.status === 'ACTIVE';
                return `
                    <div class="incident-card" onclick="focusIncident(${emg.latitude}, ${emg.longitude}, '${emg.user_id}')">
                        <div class="incident-header">
                            <span class="incident-user">${emg.user_id}</span>
                            <span class="badge ${badgeClass}">${emg.risk_level} (${emg.risk_score})</span>
                        </div>
                        <div class="incident-meta">
                            <div>Type: <b>${emg.emergency_type}</b> | Status: <b>${emg.status}</b></div>
                            <div>GPS: ${emg.latitude.toFixed(5)}° N, ${emg.longitude.toFixed(5)}° E</div>
                            <div>Signals: ${emg.contributing_factors}</div>
                        </div>
                        ${isActive ? `
                            <div class="incident-actions">
                                <button class="btn-dispatch" onclick="dispatchPatrol('${emg.emergency_id}', event)">Dispatch Patrol</button>
                                <button class="btn-resolve" onclick="resolveIncident('${emg.emergency_id}', event)">Resolve</button>
                            </div>
                        ` : ''}
                    </div>
                `;
            }).join('');

            // Update Map Markers
            emergencies.forEach(emg => {
                if (emg.status === 'ACTIVE') {
                    if (!markers[emg.user_id]) {
                        markers[emg.user_id] = L.circleMarker([emg.latitude, emg.longitude], {
                            radius: 10,
                            color: '#FFFFFF',
                            fillColor: '#D32F2F',
                            fillOpacity: 0.9,
                            weight: 3
                        }).addTo(map);
                    } else {
                        markers[emg.user_id].setLatLng([emg.latitude, emg.longitude]);
                    }
                    markers[emg.user_id].bindPopup(`<b>${emg.user_id}</b><br>Risk: ${emg.risk_level} (${emg.risk_score}/100)<br>Type: ${emg.emergency_type}`);
                }
            });
        }

        function focusIncident(lat, lng, userId) {
            map.setView([lat, lng], 17);
            if (markers[userId]) markers[userId].openPopup();
        }

        async function dispatchPatrol(id, e) {
            e.stopPropagation();
            alert("Patrol Unit dispatched to emergency ID: " + id);
        }

        async function resolveIncident(id, e) {
            e.stopPropagation();
            await fetch(`/emergency/${id}/resolve`, { method: 'POST' });
            fetchEmergencies();
        }

        setInterval(fetchEmergencies, 3000);
        fetchEmergencies();
    </script>
</body>
</html>
"""

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.main:app", host="0.0.0.0", port=8000, reload=True)
