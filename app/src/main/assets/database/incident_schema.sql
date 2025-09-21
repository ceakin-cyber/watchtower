-- Incident Database Schema
-- This file defines the database structure for the incident tracking system

CREATE TABLE IF NOT EXISTS incidents (
    id TEXT PRIMARY KEY NOT NULL,
    timestamp INTEGER NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    incident_type TEXT NOT NULL,
    location TEXT NOT NULL,
    description TEXT NOT NULL,
    evidence_attachments TEXT NOT NULL DEFAULT '[]', -- JSON array of file paths/URLs
    severity_level TEXT NOT NULL CHECK(severity_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    reported_to_authorities INTEGER NOT NULL DEFAULT 0, -- Boolean: 0 = false, 1 = true
    case_number TEXT NULL,
    
    -- Legacy fields for backwards compatibility
    type TEXT NOT NULL DEFAULT incident_type,
    dateTime TEXT NOT NULL DEFAULT (datetime(timestamp/1000, 'unixepoch'))
);

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_incidents_timestamp ON incidents(timestamp);
CREATE INDEX IF NOT EXISTS idx_incidents_type ON incidents(incident_type);
CREATE INDEX IF NOT EXISTS idx_incidents_severity ON incidents(severity_level);
CREATE INDEX IF NOT EXISTS idx_incidents_case_number ON incidents(case_number);
CREATE INDEX IF NOT EXISTS idx_incidents_reported ON incidents(reported_to_authorities);

-- Sample data (optional - for testing purposes)
/*
INSERT INTO incidents (
    id, timestamp, incident_type, location, description, 
    evidence_attachments, severity_level, reported_to_authorities, case_number
) VALUES 
(
    'sample-001',
    1640995200000, -- 2022-01-01 00:00:00 UTC
    'Security Breach',
    'Server Room A, Building 1',
    'Unauthorized access detected to main database server',
    '["evidence/photo1.jpg", "evidence/log_file.txt"]',
    'HIGH',
    1,
    'INC-2022-001'
),
(
    'sample-002',
    1640995800000, -- 2022-01-01 00:10:00 UTC
    'System Outage',
    'Data Center 2',
    'Primary web server experienced unexpected shutdown',
    '["evidence/server_logs.log", "evidence/monitoring_screenshot.png"]',
    'CRITICAL',
    1,
    'INC-2022-002'
);
*/