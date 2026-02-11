package com.university.attendance.entity;

public enum SessionStatus {
    SCHEDULED,   // Session created but not started
    ACTIVE,      // QR code being displayed
    COMPLETED,   // Session finished
    CANCELLED    // Session cancelled
}