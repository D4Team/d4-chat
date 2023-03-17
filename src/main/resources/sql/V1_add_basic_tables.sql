CREATE SCHEMA IF NOT EXISTS messaging;

-- Person
CREATE TABLE IF NOT EXISTS messaging.person
(
    person_id uuid NOT NULL,
    name VARCHAR(50) NOT NULL,
    birth_date timestamptz,
    person_info VARCHAR(300),
    CONSTRAINT person_table_pk PRIMARY KEY (person_id)
);

COMMENT ON TABLE messaging.person IS E'Person\'s data';
COMMENT ON COLUMN messaging.person.person_info IS E'Person\'s information about himself';

-- Chat_room
CREATE TABLE IF NOT EXISTS messaging.chat_room
(
    chat_id uuid NOT NULL,
    chat_name VARCHAR(100) NOT NULL,
    info text,
    CONSTRAINT chat_room_info_pk PRIMARY KEY (chat_id)
);

COMMENT ON TABLE messaging.chat_room IS E'Chat\'s channel data';
COMMENT ON COLUMN messaging.chat_room.info IS E'Chat\'s channel information from creator';

-- Messaging in chat_room
CREATE TABLE IF NOT EXISTS messaging.chat_room_messaging
(
    message_id uuid NOT NULL,
    chat_id uuid NOT NULL,
    writer_id uuid NOT NULL,
    message text,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chat_room_messaging_pk PRIMARY KEY (message_id),
    CONSTRAINT writer_id_fk FOREIGN KEY (writer_id)
        REFERENCES messaging.person (person_id)
        ON UPDATE CASCADE,
    CONSTRAINT chat_id_fk FOREIGN KEY (chat_id)
        REFERENCES messaging.chat_room (chat_id)
        ON UPDATE CASCADE
);

COMMENT ON TABLE messaging.chat_room_messaging IS 'Summary table for all chat rooms';
COMMENT ON COLUMN messaging.chat_room_messaging.writer_id IS 'ID of the person who wrote the message';

-- Members of chat_room
CREATE TABLE IF NOT EXISTS messaging.chat_room_members
(
    id uuid NOT NULL,
    chat_id uuid NOT NULL,
    member_id uuid NOT NULL,
    CONSTRAINT chat_room_members_pk PRIMARY KEY (id),
    CONSTRAINT chat_id_fk FOREIGN KEY (chat_id)
        REFERENCES messaging.chat_room (chat_id)
        ON UPDATE CASCADE,
    CONSTRAINT member_id_fk FOREIGN KEY (member_id)
        REFERENCES messaging.person (person_id)
        ON UPDATE CASCADE
);

COMMENT ON TABLE messaging.chat_room_members IS E'Table-relation between chat and it\'s participants';
COMMENT ON COLUMN messaging.chat_room_members.member_id IS 'ID of the person who participates in the chat';
