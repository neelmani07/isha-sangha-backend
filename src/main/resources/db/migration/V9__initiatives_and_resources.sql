-- Member-submitted initiatives (seva and ventures) and the resource library of useful links.
-- Both go through the same approval queue as teachers and centres.

CREATE TABLE initiative
(
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(250) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    kind VARCHAR(30) NOT NULL,
    summary VARCHAR(300) NOT NULL,
    description TEXT,
    city_id BIGINT NOT NULL REFERENCES city(id),
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    started_year INTEGER,
    started_by_name VARCHAR(120),
    -- Comma-separated: VOLUNTEERS, FUNDS, SPACE, SKILLS, MATERIALS
    needs VARCHAR(100),
    needs_note VARCHAR(1000),
    contact_whatsapp VARCHAR(30),
    contact_email VARCHAR(255),
    contact_website VARCHAR(500),
    -- Either an uploaded image key or an external image URL
    photo_key VARCHAR(120),
    photo_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    listing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    submitted_by_user_id BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_initiative_status ON initiative(listing_status, active);
CREATE INDEX idx_initiative_city ON initiative(city_id);
CREATE INDEX idx_initiative_submitted_by ON initiative(submitted_by_user_id);

-- Members who said "I support this".
CREATE TABLE initiative_supporter
(
    initiative_id BIGINT NOT NULL REFERENCES initiative(id) ON DELETE CASCADE,
    app_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (initiative_id, app_user_id)
);

CREATE TABLE resource_link
(
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(30) NOT NULL,
    -- Chat-group invites and similar are shown to signed-in members only
    members_only BOOLEAN NOT NULL DEFAULT FALSE,
    city_id BIGINT REFERENCES city(id),
    language VARCHAR(60),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    listing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    submitted_by_user_id BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_resource_status ON resource_link(listing_status, active);
CREATE INDEX idx_resource_category ON resource_link(category);
CREATE INDEX idx_resource_submitted_by ON resource_link(submitted_by_user_id);

-- Members who marked a link as helpful.
CREATE TABLE resource_vote
(
    resource_id BIGINT NOT NULL REFERENCES resource_link(id) ON DELETE CASCADE,
    app_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (resource_id, app_user_id)
);
