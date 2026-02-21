ALTER TABLE stores
    ADD COLUMN address      VARCHAR(200) NOT NULL DEFAULT '' AFTER lng,
    ADD COLUMN detail_address VARCHAR(100) NULL     AFTER address;

ALTER TABLE stores
    ALTER COLUMN address DROP DEFAULT;
