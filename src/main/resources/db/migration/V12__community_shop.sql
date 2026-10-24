-- Community shop: volunteers list things they make or grow; buyers contact the seller directly.
-- The site never handles payment. Listings are reviewed before they appear.
CREATE TABLE shop_listing
(
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(250) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description TEXT,
    price_amount DECIMAL(10,2),
    price_note VARCHAR(60),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    delivery VARCHAR(30) NOT NULL DEFAULT 'PICKUP',
    city_id BIGINT NOT NULL REFERENCES city(id),
    seller_name VARCHAR(120) NOT NULL,
    contact_whatsapp VARCHAR(30),
    contact_phone VARCHAR(30),
    contact_email VARCHAR(255),
    contact_website VARCHAR(500),
    photo_key VARCHAR(120),
    photo_url VARCHAR(500),
    -- Seller confirmed the item is not Isha-branded, not presented as consecrated, and allowed to sell
    rules_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    listing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    submitted_by_user_id BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shop_status ON shop_listing(listing_status, active);
CREATE INDEX idx_shop_submitted_by ON shop_listing(submitted_by_user_id);
