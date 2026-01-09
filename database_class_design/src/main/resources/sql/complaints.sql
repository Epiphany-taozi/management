CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status ENUM('NEW', 'IN_PROGRESS', 'RESOLVED') NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_complaints_owner_id (owner_id),
    CONSTRAINT fk_complaints_owner
        FOREIGN KEY (owner_id) REFERENCES owners(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS complaint_replies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    complaint_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    reply_content TEXT NOT NULL,
    reply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_complaint_replies_complaint_id (complaint_id),
    INDEX idx_complaint_replies_staff_id (staff_id),
    CONSTRAINT fk_complaint_reply_complaint
        FOREIGN KEY (complaint_id) REFERENCES complaints(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_complaint_reply_staff
        FOREIGN KEY (staff_id) REFERENCES staff(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);
