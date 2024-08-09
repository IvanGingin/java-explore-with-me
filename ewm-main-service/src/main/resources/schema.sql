DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS compilations CASCADE;
DROP TABLE IF EXISTS compilation_events CASCADE;
DROP TABLE IF EXISTS requests CASCADE;

CREATE TABLE IF NOT EXISTS users (
     id BIGSERIAL PRIMARY KEY,
     name VARCHAR(250) NOT NULL,
    email VARCHAR(254) UNIQUE NOT NULL
    );

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
    );

CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    lat FLOAT8,
    lon FLOAT8
);

CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    annotation VARCHAR(2000) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    created_on TIMESTAMP WITHOUT TIME ZONE,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE,
    initiator_id BIGINT REFERENCES users(id),
    location_id BIGINT NOT NULL REFERENCES locations(id),
    paid BOOLEAN,
    participant_limit INTEGER,
    published_on TIMESTAMP,
    request_moderation BOOLEAN,
    state VARCHAR(10),
    title VARCHAR(120) NOT NULL,
    confirmed_requests BIGINT,
    views BIGINT
    );

CREATE TABLE IF NOT EXISTS compilations (
    id BIGSERIAL PRIMARY KEY,
    pinned BOOLEAN,
    title VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT REFERENCES compilations(id),
    event_id BIGINT REFERENCES events(id),
    PRIMARY KEY (compilation_id, event_id)
    );

CREATE TABLE IF NOT EXISTS requests (
      id BIGSERIAL PRIMARY KEY,
      created TIMESTAMP,
      event_id BIGINT NOT NULL REFERENCES events(id),
    requester_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50)
    );
