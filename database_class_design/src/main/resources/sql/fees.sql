CREATE TABLE fees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    fee_type VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    paid_at DATETIME,
    status VARCHAR(20) NOT NULL,

    INDEX idx_fees_owner_id (owner_id),
    INDEX idx_fees_paid_at (paid_at),
    INDEX idx_fees_fee_type (fee_type),

    CONSTRAINT fk_fees_owner
        FOREIGN KEY (owner_id)
        REFERENCES owners (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

SELECT
    YEAR(paid_at) AS paid_year,
    fee_type,
    SUM(amount) AS total_amount,
    COUNT(*) AS total_count
FROM fees
WHERE paid_at IS NOT NULL
GROUP BY YEAR(paid_at), fee_type
ORDER BY paid_year, fee_type;
