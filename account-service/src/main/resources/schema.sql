-- Branch Table
CREATE TABLE IF NOT EXISTS branches (
    branch_id SERIAL PRIMARY KEY,
    branch_name VARCHAR(100) NOT NULL,
    branch_code VARCHAR(10) NOT NULL UNIQUE,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zipcode VARCHAR(10) NOT NULL
);

-- Account Table
CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(9) PRIMARY KEY CHECK (account_number ~ '^5[0-9]{8}$'),
    customer_id BIGINT NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    routing_number VARCHAR(9) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    branch_id INTEGER NOT NULL REFERENCES branches(branch_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert some default branches
INSERT INTO branches (branch_name, branch_code, city, state, zipcode) 
VALUES 
    ('Main Branch', 'MB001', 'New York', 'NY', '10001'),
    ('Downtown Branch', 'DB002', 'Chicago', 'IL', '60601'),
    ('West Coast Branch', 'WC003', 'San Francisco', 'CA', '94105')
ON CONFLICT (branch_code) DO NOTHING;