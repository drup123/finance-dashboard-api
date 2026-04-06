package com.finance.dashboard.model;

public enum Role {
    VIEWER,    // Read-only: can view dashboard data
    ANALYST,   // Can view records and access summaries/insights
    ADMIN      // Full access: manage records and users
}
