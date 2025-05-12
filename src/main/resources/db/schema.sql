CREATE DATABASE IF NOT EXISTS currencyconverter;
USE currencyconverter;

CREATE TABLE currencies (
    code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- Insert all supported currencies
INSERT INTO currencies (code, name, description) VALUES
('USD', 'US Dollar', 'United States Dollar'),
('EUR', 'Euro', 'European Union Euro'),
('GBP', 'British Pound', 'Great Britain Pound Sterling'),
('JPY', 'Japanese Yen', 'Japanese Yen'),
('AUD', 'Australian Dollar', 'Australian Dollar'),
('CAD', 'Canadian Dollar', 'Canadian Dollar'),
('CHF', 'Swiss Franc', 'Swiss Franc'),
('CNY', 'Chinese Yuan', 'Chinese Yuan Renminbi'),
('INR', 'Indian Rupee', 'Indian Rupee'),
('NZD', 'New Zealand Dollar', 'New Zealand Dollar'),
('SGD', 'Singapore Dollar', 'Singapore Dollar'),
('HKD', 'Hong Kong Dollar', 'Hong Kong Dollar'),
('SEK', 'Swedish Krona', 'Swedish Krona'),
('KRW', 'South Korean Won', 'South Korean Won'),
('MXN', 'Mexican Peso', 'Mexican Peso'),
('ZAR', 'South African Rand', 'South African Rand');

CREATE TABLE job_execution_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    records_processed INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE currency_exchange_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_execution_id BIGINT NOT NULL,
    base_currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(20,6) NOT NULL,
    rate_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_execution_id) REFERENCES job_execution_history(id),
    INDEX idx_base_target_currency (base_currency, target_currency),
    INDEX idx_rate_timestamp (rate_timestamp)
);
