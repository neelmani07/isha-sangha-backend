-- Every row added as preview sample data (DemoContentSeeder) is listed here, so it can all be removed in one go
-- with DEMO_CONTENT=remove without touching anything real members or admins added.
CREATE TABLE demo_content
(
    entity_type VARCHAR(30) NOT NULL,
    entity_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (entity_type, entity_id)
);
