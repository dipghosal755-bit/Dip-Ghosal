import pytest
from fastapi.testclient import TestClient
from backend.main import app

client = TestClient(app)

def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

def test_trigger_sos():
    payload = {
        "user_id": "TEST_STUDENT_01",
        "latitude": 28.6139,
        "longitude": 77.2090,
        "emergency_type": "MANUAL_SOS",
        "is_fall_detected": True,
        "is_no_movement": False,
        "is_route_deviation": False
    }
    response = client.post("/sos", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["user_id"] == "TEST_STUDENT_01"
    assert data["status"] == "ACTIVE"
    # Manual SOS (50) + Fall detected (25) = 75 -> CRITICAL
    assert data["risk_score"] == 75
    assert data["risk_level"] == "CRITICAL"

def test_update_location():
    # Trigger SOS first
    sos_res = client.post("/sos", json={
        "user_id": "TEST_USER_TRACK",
        "latitude": 28.6139,
        "longitude": 77.2090,
        "emergency_type": "MANUAL_SOS"
    })
    emg_id = sos_res.json()["emergency_id"]

    loc_payload = {
        "emergency_id": emg_id,
        "user_id": "TEST_USER_TRACK",
        "latitude": 28.6145,
        "longitude": 77.2095,
        "accuracy": 4.0
    }
    loc_res = client.post("/location", json=loc_payload)
    assert loc_res.status_code == 200

    history_res = client.get("/locations/TEST_USER_TRACK")
    assert history_res.status_code == 200
    assert len(history_res.json()) >= 1

def test_cancel_sos():
    sos_res = client.post("/sos", json={
        "user_id": "TEST_CANCEL_USER",
        "latitude": 28.6139,
        "longitude": 77.2090,
        "emergency_type": "MANUAL_SOS"
    })
    emg_id = sos_res.json()["emergency_id"]

    cancel_res = client.post(f"/emergency/{emg_id}/cancel")
    assert cancel_res.status_code == 200
    assert cancel_res.json()["status"] == "CANCELLED"
