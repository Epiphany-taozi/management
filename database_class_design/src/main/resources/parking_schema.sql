-- Parking slots table
CREATE TABLE parking_slots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    location VARCHAR(255) NOT NULL,
    is_occupied BOOLEAN NOT NULL DEFAULT FALSE
);

-- Parking usage records table
CREATE TABLE parking_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    slot_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    INDEX idx_parking_usage_slot_id (slot_id),
    INDEX idx_parking_usage_owner_id (owner_id),
    CONSTRAINT fk_parking_usage_slot
        FOREIGN KEY (slot_id) REFERENCES parking_slots(id),
    CONSTRAINT fk_parking_usage_owner
        FOREIGN KEY (owner_id) REFERENCES owners(id)
);

-- Annual statistics: usage count and total fee grouped by year
SELECT
    YEAR(start_time) AS usage_year,
    COUNT(*) AS usage_count,
    SUM(fee) AS total_fee
FROM parking_usage
GROUP BY YEAR(start_time)
ORDER BY usage_year;
