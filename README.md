Nastaveni sql databaze : 
CREATE TABLE bookings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    room_id INT,
    booking_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE TABLE room_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_type_id INT,
    description TEXT,
    price DECIMAL(10, 2),
    -- Další sloupce podle potřeby
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);



CREATE TABLE rooms (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type_id INT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (type_id) REFERENCES room_types(id)
);

CREATE TABLE room_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    jmeno VARCHAR(255) NOT NULL,
    prijmeni VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    heslo VARCHAR(255) NOT NULL,
    telefonni_cislo VARCHAR(20) NOT NULL
);




INSERT INTO room_types (name) VALUES ('Single Room'), ('Double Room'), ('Suite'), ('Deluxe Suite');

INSERT INTO rooms (type_id) VALUES (1), (1), (1), (1), (1), (1), (1), (1), (1), (1),
                                   (2), (2), (2), (2), (2), (2), (2), (2), (2), (2),
                                   (3), (3), (3), (3), (3), (3), (3), (3), (3), (3),
                                   (4), (4), (4), (4), (4), (4), (4), (4), (4), (4);

-- Přidání detailů pro konkrétní typ pokoje
INSERT INTO room_details (room_type_id, description, price)
VALUES (1, 'Single room with ocean view', 100.00),
       (2, 'Double room with city view', 170.00),
       (3, 'Suite with jacuzzi and mountain view', 210.00),
	   (4, 'Deluxe room with jungle view', 230.00);

