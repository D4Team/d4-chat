--liquibase formatted sql

--changeset Koshelev.OA:insert_data_and_add_field
INSERT INTO dummy(dummy_field)
VALUES ('dummy_1');
ALTER TABLE dummy
ADD COLUMN created  timestamptz DEFAULT CURRENT_TIMESTAMP;
--rollback DELETE FROM dummy WHERE dummu_fileld = 'dummy_1'
--rollback ALTER TABLE dummy
--rollback DROP COLUMN created;


--changeset Koshelev.OA:insert_data
INSERT INTO dummy(dummy_field)
VALUES ('dummy_2');
--rollback DELETE FROM dummy WHERE dummy_fileld = 'dummy_2'