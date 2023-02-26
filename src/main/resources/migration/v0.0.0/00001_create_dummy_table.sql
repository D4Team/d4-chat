--liquibase formatted sql
--changeset Koshelev.OA:create_dummy_table
CREATE TABLE dummy(
    dummy_field VARCHAR(42)
);
--rollback DROP TABLE
--rollback dummy