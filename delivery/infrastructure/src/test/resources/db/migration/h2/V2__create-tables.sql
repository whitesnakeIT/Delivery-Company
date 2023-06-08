use test;

DROP TABLE IF EXISTS couriers cascade;
DROP TABLE IF EXISTS customers cascade;
DROP TABLE IF EXISTS deliveries cascade;
DROP TABLE IF EXISTS orders cascade;
DROP TABLE IF EXISTS tracking cascade;


CREATE TABLE couriers
(
    courier_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    courier_company VARCHAR(255),
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    version         INT,
    uuid UUID
);

CREATE TABLE customers
(
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    email       VARCHAR(255),
    version     INT,
    uuid UUID
);


CREATE TABLE orders
(
    order_id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_address          VARCHAR(255),
    receiver_address        VARCHAR(255),
    package_type            VARCHAR(255),
    package_size            VARCHAR(255),
    preferred_delivery_date DATE,
    version                 INT,
    uuid UUID,
    customer_id             BIGINT NOT NULL,
    CONSTRAINT FK_CustomerOrder
        FOREIGN KEY (customer_id)
            REFERENCES customers (customer_id)

);

CREATE TABLE tracking
(
    tracking_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    localization VARCHAR(255),
    version      INT,
    uuid UUID
);

CREATE TABLE deliveries
(
    delivery_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_status VARCHAR(255),
    delivery_time   TIMESTAMP(6),
    price           NUMERIC(38, 2),
    version         INT,
    uuid UUID,
    courier_id      BIGINT,
    order_id        BIGINT,
    CONSTRAINT FK_Delivery_Courier
        FOREIGN KEY (courier_id)
            REFERENCES couriers (courier_id),
    CONSTRAINT FK_Delivery_Order
        FOREIGN KEY (order_id)
            REFERENCES orders (order_id)
);