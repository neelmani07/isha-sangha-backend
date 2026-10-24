-- Review trail for self-submitted listings.
ALTER TABLE center
    ADD COLUMN rejection_reason VARCHAR(500),
    ADD COLUMN reviewed_at TIMESTAMP,
    ADD COLUMN reviewed_by VARCHAR(255);

ALTER TABLE teacher
    ADD COLUMN rejection_reason VARCHAR(500),
    ADD COLUMN reviewed_at TIMESTAMP,
    ADD COLUMN reviewed_by VARCHAR(255);

CREATE INDEX idx_center_submitted_by ON center(submitted_by_user_id);
CREATE INDEX idx_teacher_submitted_by ON teacher(submitted_by_user_id);
