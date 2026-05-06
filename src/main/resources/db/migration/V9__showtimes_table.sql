CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TYPE showtime_status AS ENUM ('SCHEDULED', 'CANCELLED');

CREATE TABLE IF NOT EXISTS show_times(
                                         id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    room_id INT NOT NULL,
    starts_at timestamptz NOT NULL,
    ends_at timestamptz NOT NULL,
    price BIGINT NOT NULL,
    status showtime_status NOT NULL DEFAULT 'SCHEDULED',
    EXCLUDE USING gist (room_id WITH =, tstzrange(starts_at, ends_at) WITH &&), -- no room overlap

    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);