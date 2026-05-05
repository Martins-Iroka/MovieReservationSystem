CREATE TABLE IF NOT EXISTS seats(
    id INT PRIMARY KEY,
    room_id INT NOT NULL,
    row_label TEXT NOT NULL,
    seat_number INT NOT NULL,

    UNIQUE (room_id, row_label, seat_number),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);