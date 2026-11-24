CREATE TABLE IF NOT EXISTS lab_trade_brand (
  id BIGINT PRIMARY KEY, brand_name VARCHAR(60) NOT NULL UNIQUE,
  website VARCHAR(160)
);
CREATE TABLE IF NOT EXISTS lab_trade_product (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_name VARCHAR(100) NOT NULL, category VARCHAR(40) NOT NULL,
  brand VARCHAR(60) NOT NULL, price DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL, sale_status VARCHAR(20) NOT NULL,
  brand_id BIGINT NOT NULL, version INT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_trade_product_brand FOREIGN KEY (brand_id) REFERENCES lab_trade_brand(id),
  CONSTRAINT ck_trade_price CHECK (price >= 0),
  CONSTRAINT ck_trade_stock CHECK (stock >= 0),
  CONSTRAINT ck_trade_status CHECK (sale_status IN ('ON_SALE', 'OFF_SALE'))
);
CREATE INDEX idx_trade_product_name ON lab_trade_product(product_name);
CREATE TABLE IF NOT EXISTS lab_trade_customer (
  id BIGINT PRIMARY KEY, customer_name VARCHAR(60) NOT NULL, city VARCHAR(40) NOT NULL
);
CREATE TABLE IF NOT EXISTS lab_trade_order (
  id BIGINT PRIMARY KEY, order_no VARCHAR(40) NOT NULL UNIQUE,
  customer_id BIGINT NOT NULL, ordered_on DATE NOT NULL, order_status VARCHAR(20) NOT NULL,
  FOREIGN KEY (customer_id) REFERENCES lab_trade_customer(id)
);
CREATE TABLE IF NOT EXISTS lab_trade_order_line (
  id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
  quantity INT NOT NULL, deal_price DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES lab_trade_order(id),
  FOREIGN KEY (product_id) REFERENCES lab_trade_product(id)
);

INSERT INTO lab_trade_brand VALUES (1,'远川','https://example.com'),(2,'禾木',NULL);
INSERT INTO lab_trade_product (id,product_name,category,brand,price,stock,sale_status,brand_id)
VALUES (1,'机械键盘','数码','远川',299.00,15,'ON_SALE',1),
       (2,'无线鼠标','数码','远川',89.00,28,'ON_SALE',1),
       (3,'收纳袋','家居','禾木',29.90,0,'OFF_SALE',2);
INSERT INTO lab_trade_customer VALUES (1,'示例客户','天津');
INSERT INTO lab_trade_order VALUES (1,'LAB-0001',1,'2026-11-24','PAID');
INSERT INTO lab_trade_order_line VALUES (1,1,1,1,299.00);
