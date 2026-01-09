-- =========================
-- Repairs: 报修主表（当前状态 + 费用 + 时间）
-- =========================
CREATE TABLE IF NOT EXISTS repairs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,

  owner_id BIGINT NOT NULL,              -- 报修所属业主
  type VARCHAR(50) NOT NULL,             -- 报修类型（如水电/门锁/电梯等）
  title VARCHAR(255) NOT NULL,           -- 简短标题
  description TEXT,                      -- 详细描述

  status VARCHAR(30) NOT NULL,           -- 当前状态（如 NEW/IN_PROGRESS/DONE/CLOSED）
  cost DECIMAL(10, 2) NOT NULL DEFAULT 0,

  requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME NULL,

  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_repairs_owner
    FOREIGN KEY (owner_id)
    REFERENCES owners (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- Repair logs: 报修处理日志（历史记录）
-- =========================
CREATE TABLE IF NOT EXISTS repair_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repair_id BIGINT NOT NULL,
  handler VARCHAR(100) NOT NULL,
  action VARCHAR(100) NOT NULL,
  action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_repair_logs_repair
    FOREIGN KEY (repair_id)
    REFERENCES repairs (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =========================
-- 查询：带可选 status 过滤 + 返回“最后处理时间”
-- :status 可为空（为空返回全部）
-- =========================
SELECT
  r.id,
  r.owner_id,
  r.type,
  r.title,
  r.status,
  r.cost,
  r.requested_at,
  r.completed_at,
  COALESCE(MAX(l.action_time), r.updated_at) AS last_action_time
FROM repairs r
LEFT JOIN repair_logs l ON l.repair_id = r.id
WHERE (:status IS NULL OR r.status = :status)
GROUP BY
  r.id, r.owner_id, r.type, r.title, r.status, r.cost,
  r.requested_at, r.completed_at, r.updated_at
ORDER BY last_action_time DESC;

-- =========================
-- 统计：按年份统计已完成报修数量与总费用（不存 completed_year）
-- =========================
SELECT
  YEAR(completed_at) AS completed_year,
  COUNT(*) AS total_repairs,
  SUM(cost) AS total_cost
FROM repairs
WHERE completed_at IS NOT NULL
GROUP BY YEAR(completed_at)
ORDER BY completed_year;

-- =========================
-- 插入：completed_at 为空表示未完成
-- =========================
INSERT INTO repairs (
  owner_id, type, title, description, status, cost, requested_at, completed_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?);
