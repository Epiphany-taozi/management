CREATE TABLE repairs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL DEFAULT 0,
    requested_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    completed_year INT NULL,
    CONSTRAINT fk_repairs_owner FOREIGN KEY (owner_id) REFERENCES owner (id)
);

SELECT YEAR(completed_at) AS completed_year,
       COUNT(*) AS total_repairs,
       SUM(cost) AS total_cost
FROM repairs
WHERE completed_at IS NOT NULL
GROUP BY YEAR(completed_at)
ORDER BY completed_year;

SELECT completed_year,
       COUNT(*) AS total_repairs,
       SUM(cost) AS total_cost
FROM repairs
WHERE completed_year IS NOT NULL
GROUP BY completed_year
ORDER BY completed_year;

INSERT INTO repairs (
    owner_id,
    type,
    status,
    cost,
    requested_at,
    completed_at,
    completed_year
) VALUES (?, ?, ?, ?, ?, ?, YEAR(?));
