-- Schema for Hotel Management (MySQL 8+)
-- Character set: utf8mb4 for full Unicode

CREATE DATABASE IF NOT EXISTS hotel_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE hotel_management;

-- ENUMs stored as VARCHAR for readability and compatibility with JDBC
-- Money stored as DECIMAL(12,2) with currency column (default VND)

CREATE TABLE rooms (
    room_id           VARCHAR(20)  NOT NULL,
    room_name         VARCHAR(100) NOT NULL,
    room_type         VARCHAR(30)  NOT NULL,
    bed_type          VARCHAR(30)  NOT NULL,
    price_amount      DECIMAL(12,2) NOT NULL,
    currency          VARCHAR(3)   NOT NULL DEFAULT 'VND',
    status            VARCHAR(20)  NOT NULL,
    created_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (room_id),
    INDEX idx_rooms_status (status),
    INDEX idx_rooms_name (room_name),
    INDEX idx_rooms_type (room_type)
) ENGINE=InnoDB;

CREATE TABLE customers (
    customer_id   VARCHAR(36) NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    identity_no   VARCHAR(30),
    created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (customer_id),
    UNIQUE KEY uq_customers_phone (phone),
    UNIQUE KEY uq_customers_identity (identity_no)
) ENGINE=InnoDB;

CREATE TABLE bookings (
    booking_id     VARCHAR(36) NOT NULL,
    room_id        VARCHAR(20) NOT NULL,
    customer_id    VARCHAR(36) NOT NULL,
    check_in_date  DATE        NOT NULL,
    check_out_date DATE        NOT NULL,
    status         VARCHAR(20) NOT NULL,
    created_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (booking_id),
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    CONSTRAINT fk_bookings_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT chk_booking_dates CHECK (check_out_date > check_in_date),
    INDEX idx_bookings_room (room_id),
    INDEX idx_bookings_customer (customer_id),
    INDEX idx_bookings_status (status),
    INDEX idx_bookings_dates (check_in_date, check_out_date)
) ENGINE=InnoDB;

CREATE TABLE services (
    service_id        VARCHAR(20) NOT NULL,
    name              VARCHAR(120) NOT NULL,
    unit_price_amount DECIMAL(12,2) NOT NULL,
    currency          VARCHAR(3) NOT NULL DEFAULT 'VND',
    stock             INT NOT NULL DEFAULT 0,
    is_active         TINYINT(1) NOT NULL DEFAULT 1,
    created_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (service_id),
    INDEX idx_services_name (name),
    INDEX idx_services_active (is_active),
    INDEX idx_services_stock (stock)
) ENGINE=InnoDB;

CREATE TABLE service_usages (
    usage_id   VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NOT NULL,
    service_id VARCHAR(20) NOT NULL,
    quantity   INT NOT NULL,
    used_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (usage_id),
    CONSTRAINT fk_usage_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_usage_service FOREIGN KEY (service_id) REFERENCES services(service_id),
    INDEX idx_usage_booking (booking_id),
    INDEX idx_usage_service (service_id),
    INDEX idx_usage_used_at (used_at)
) ENGINE=InnoDB;

CREATE TABLE invoices (
    invoice_id            VARCHAR(36) NOT NULL,
    booking_id            VARCHAR(36) NOT NULL,
    room_total_amount     DECIMAL(12,2) NOT NULL,
    services_total_amount DECIMAL(12,2) NOT NULL,
    grand_total_amount    DECIMAL(12,2) NOT NULL,
    currency              VARCHAR(3) NOT NULL DEFAULT 'VND',
    status                VARCHAR(20) NOT NULL,
    paid_at               DATETIME(3) NOT NULL,
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (invoice_id),
    UNIQUE KEY uq_invoice_booking (booking_id),
    CONSTRAINT fk_invoice_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    INDEX idx_invoice_status (status),
    INDEX idx_invoice_paid_at (paid_at)
) ENGINE=InnoDB;

-- Note: enforce no overlapping ACTIVE bookings per room via application logic with supporting indexes above.
