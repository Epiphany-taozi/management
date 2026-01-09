-- Repairs table stores current status of a repair request.
CREATE TABLE IF NOT EXISTS repairs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Repair logs store processing history for a repair request.
CREATE TABLE IF NOT EXISTS repair_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repair_id BIGINT NOT NULL,
  handler VARCHAR(100) NOT NULL,
  action VARCHAR(100) NOT NULL,
  action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (repair_id) REFERENCES repairs(id)
);

-- Query repairs with optional status filter, returning current status and last action time.
-- :status is optional; when NULL it returns all rows.
SELECT
  r.id,
  r.title,
  r.status,
  COALESCE(MAX(l.action_time), r.updated_at) AS last_action_time
FROM repairs r
LEFT JOIN repair_logs l ON l.repair_id = r.id
WHERE (:status IS NULL OR r.status = :status)
GROUP BY r.id, r.title, r.status, r.updated_at
ORDER BY last_action_time DESC;
