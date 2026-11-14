-- Schema for Generalized Event/Venue Cab-Sharing Platform.
-- Supports Airports, Stadiums, Concerts, Tech Parks, and Campus Venues.
-- Loaded automatically by docker-compose on first start.

CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- for gen_random_uuid()

-- ---------------------------------------------------------------
-- Users: Accounts with gender verification for women-only pools.
-- ---------------------------------------------------------------
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    phone       VARCHAR(20),
    gender      VARCHAR(10)   CHECK (gender IN ('FEMALE', 'MALE', 'OTHER')),
    verified    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------
-- Ride requests: One row per person asking for a shared ride.
-- Generalized to any venue (Airport, Concert, Stadium, Campus)
-- and direction (FROM_VENUE or TO_VENUE).
-- version = optimistic locking for updates to this record.
-- ---------------------------------------------------------------
CREATE TABLE ride_requests (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID          NOT NULL REFERENCES users(id),
    venue             VARCHAR(50)   NOT NULL,            -- e.g. KIA_AIRPORT, CHINNASWAMY_STADIUM, MANYATA_TECH_PARK
    direction         VARCHAR(15)   NOT NULL DEFAULT 'FROM_VENUE'
                      CHECK (direction IN ('FROM_VENUE', 'TO_VENUE')),
    event_identifier  VARCHAR(60),                       -- e.g. Flight 6E-204, Coldplay Concert, IPL RCB vs CSK
    target_time       TIMESTAMPTZ   NOT NULL,            -- Landing time, concert finish, or shift departure
    max_wait_minutes  INT           NOT NULL DEFAULT 30,
    drop_address      TEXT          NOT NULL,
    drop_lat          DOUBLE PRECISION NOT NULL,
    drop_lng          DOUBLE PRECISION NOT NULL,
    drop_zone         VARCHAR(40)   NOT NULL,            -- e.g. HSR, WHITEFIELD, INDIRANAGAR
    preference        VARCHAR(12)   NOT NULL DEFAULT 'MIXED'
                      CHECK (preference IN ('MIXED', 'WOMEN_ONLY')),
    status            VARCHAR(12)   NOT NULL DEFAULT 'OPEN'
                      CHECK (status IN ('OPEN', 'MATCHED', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    version           BIGINT        NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_requests_open
    ON ride_requests (venue, direction, status, target_time);
CREATE INDEX idx_requests_zone
    ON ride_requests (drop_zone) WHERE status = 'OPEN';

-- ---------------------------------------------------------------
-- Ride groups: A proposed or confirmed shared cab (2-4 people).
-- ---------------------------------------------------------------
CREATE TABLE ride_groups (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venue               VARCHAR(50)   NOT NULL,
    direction           VARCHAR(15)   NOT NULL DEFAULT 'FROM_VENUE',
    women_only          BOOLEAN       NOT NULL DEFAULT FALSE,
    status              VARCHAR(12)   NOT NULL DEFAULT 'PROPOSED'
                        CHECK (status IN ('PROPOSED', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    planned_pickup_time TIMESTAMPTZ,
    est_total_fare      NUMERIC(10,2),
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------
-- Group members: Who is in which group, drop order, fare split.
-- UNIQUE(request_id) PREVENTS DOUBLE MATCHING AT INSERT TIME.
-- (Explaining this: @Version prevents concurrent UPDATE overwrites,
-- but UNIQUE constraint prevents concurrent INSERT conflicts).
-- ---------------------------------------------------------------
CREATE TABLE ride_group_members (
    group_id     UUID          NOT NULL REFERENCES ride_groups(id) ON DELETE CASCADE,
    request_id   UUID          NOT NULL UNIQUE REFERENCES ride_requests(id),
    drop_order   INT,
    share_amount NUMERIC(10,2),
    accepted     BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (group_id, request_id)
);

-- ---------------------------------------------------------------
-- Transactional Outbox (Solves Save-Then-Publish dual-write gap):
-- Events written in the same DB transaction as ride_requests.
-- ---------------------------------------------------------------
CREATE TABLE outbox_events (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id   VARCHAR(50) NOT NULL,
    topic          VARCHAR(100) NOT NULL,
    payload        TEXT NOT NULL,
    status         VARCHAR(15) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
