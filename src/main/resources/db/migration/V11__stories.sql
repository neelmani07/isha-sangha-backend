-- Member stories (Magic of Devi, transformation, sadhana, seva) with likes; reviewed before they appear.
CREATE TABLE story
(
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(250) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(30) NOT NULL,
    excerpt VARCHAR(400) NOT NULL,
    body TEXT NOT NULL,
    author_name VARCHAR(120) NOT NULL,
    city_id BIGINT REFERENCES city(id),
    center_id BIGINT REFERENCES center(id),
    photo_key VARCHAR(120),
    photo_url VARCHAR(500),
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    listing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    submitted_by_user_id BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_story_status ON story(listing_status, active);
CREATE INDEX idx_story_submitted_by ON story(submitted_by_user_id);

CREATE TABLE story_like
(
    story_id BIGINT NOT NULL REFERENCES story(id) ON DELETE CASCADE,
    app_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (story_id, app_user_id)
);
