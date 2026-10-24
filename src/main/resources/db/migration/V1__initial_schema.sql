CREATE TABLE country
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    iso_code VARCHAR(10) NOT NULL UNIQUE,
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_country_name ON country(name);

CREATE TABLE state
(
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT NOT NULL REFERENCES country(id),
    name VARCHAR(150) NOT NULL,
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_state_country ON state(country_id);
CREATE INDEX idx_state_name ON state(name);

CREATE TABLE city
(
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT NOT NULL REFERENCES country(id),
    state_id BIGINT NOT NULL REFERENCES state(id),
    slug VARCHAR(250) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    description TEXT,
    hero_image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_city_name ON city(name);
CREATE INDEX idx_city_slug ON city(slug);
CREATE INDEX idx_city_country ON city(country_id);
CREATE INDEX idx_city_state ON city(state_id);

CREATE TABLE community_stats
(
    id BIGSERIAL PRIMARY KEY,
    city_id BIGINT NOT NULL UNIQUE REFERENCES city(id),
    meditator_count INTEGER NOT NULL DEFAULT 0,
    volunteer_count INTEGER NOT NULL DEFAULT 0,
    teacher_count INTEGER NOT NULL DEFAULT 0,
    yantra_count INTEGER NOT NULL DEFAULT 0,
    sannidhi_count INTEGER NOT NULL DEFAULT 0,
    center_count INTEGER NOT NULL DEFAULT 0,
    temple_count INTEGER NOT NULL DEFAULT 0,
    shop_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE center
(
    id BIGSERIAL PRIMARY KEY,
    city_id BIGINT NOT NULL REFERENCES city(id),
    slug VARCHAR(250) NOT NULL UNIQUE,
    name VARCHAR(250) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    address TEXT,
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_center_city ON center(city_id);
CREATE INDEX idx_center_type ON center(type);

CREATE TABLE teacher
(
    id BIGSERIAL PRIMARY KEY,
    city_id BIGINT NOT NULL REFERENCES city(id),
    slug VARCHAR(250) NOT NULL UNIQUE,
    name VARCHAR(250) NOT NULL,
    bio TEXT,
    photo_url VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_teacher_city ON teacher(city_id);
CREATE INDEX idx_teacher_name ON teacher(name);

CREATE TABLE program
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(250) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE teacher_program
(
    teacher_id BIGINT NOT NULL REFERENCES teacher(id),
    program_id BIGINT NOT NULL REFERENCES program(id),
    PRIMARY KEY (teacher_id, program_id)
);

CREATE TABLE social_link
(
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_social_link_entity ON social_link(entity_type, entity_id);

CREATE TABLE media
(
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    title VARCHAR(255),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_entity ON media(entity_type, entity_id);

CREATE TABLE feedback
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    type VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admin_user
(
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(500) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
