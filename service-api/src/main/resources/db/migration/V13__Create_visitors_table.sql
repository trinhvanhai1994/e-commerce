-- Create visitors table to track website visitors
CREATE TABLE IF NOT EXISTS visitors (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referrer TEXT,
    page_path VARCHAR(500),
    first_visit_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_visit_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    visit_count INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on session_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_visitors_session_id ON visitors(session_id);

-- Create index on first_visit_at for date-based queries
CREATE INDEX IF NOT EXISTS idx_visitors_first_visit_at ON visitors(first_visit_at);

-- Create index on last_visit_at for recent visitor queries
CREATE INDEX IF NOT EXISTS idx_visitors_last_visit_at ON visitors(last_visit_at);

