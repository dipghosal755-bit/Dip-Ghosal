from pydantic import BaseModel, Field
from typing import Optional, List
import time

class SosRequest(BaseModel):
    user_id: str
    latitude: float
    longitude: float
    emergency_type: str = "MANUAL_SOS"
    is_fall_detected: bool = False
    is_no_movement: bool = False
    is_route_deviation: bool = False

class LocationUpdateRequest(BaseModel):
    emergency_id: str
    user_id: str
    latitude: float
    longitude: float
    accuracy: float = 5.0
    timestamp: int = Field(default_factory=lambda: int(time.time() * 1000))

class EmergencyRecord(BaseModel):
    emergency_id: str
    user_id: str
    latitude: float
    longitude: float
    emergency_type: str
    risk_score: int
    risk_level: str  # LOW, MEDIUM, HIGH, CRITICAL
    status: str      # ACTIVE, CANCELLED, RESOLVED
    contributing_factors: str
    timestamp: int
    last_updated: int

class CancelResponse(BaseModel):
    emergency_id: str
    status: str
    message: str

class ContactCreate(BaseModel):
    user_id: str
    name: str
    phone: str
    relationship: str
