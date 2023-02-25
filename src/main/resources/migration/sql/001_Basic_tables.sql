CREATE SCHEMA messaging;
CREATE TABLE IF NOT EXISTS messaging.person
(
    person_id uuid NOT NULL,
    name character varying(50) NOT NULL,
    date_of_birth timestamp with time zone,
    person_info character varying(300),
    CONSTRAINT person_table_pk PRIMARY KEY (person_id)
);
COMMENT ON TABLE messaging.person IS 'Данные пользователя';
COMMENT ON COLUMN messaging.person.person_info IS 'Информация в вольном стиле от пользователя о себе';
CREATE TABLE IF NOT EXISTS messaging.dialog
(
    message_id uuid NOT NULL,
    dialog_id uuid NOT NULL,
    writer_id uuid NOT NULL,
    reader_id uuid NOT NULL,
    create_time timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    text text,
    CONSTRAINT dialog_table_pk PRIMARY KEY (message_id),
    CONSTRAINT companion_id_fk FOREIGN KEY (reader_id)
        REFERENCES messaging.person (person_id),
    CONSTRAINT user_id_fk FOREIGN KEY (writer_id)
        REFERENCES messaging.person (person_id)
);
COMMENT ON TABLE messaging.dialog IS 'Сводная информация по чатам с двумя участниками';
COMMENT ON COLUMN messaging.dialog.dialog_id IS 'Идентификатор чата для упрощенного поиска истории сообщения - формируется после отправки первого сообщения одним из участников';
COMMENT ON COLUMN messaging.dialog.writer_id IS 'Идентификатор пользователя написавшего сообщение';
COMMENT ON COLUMN messaging.dialog.reader_id IS 'Идентификатор пользователя прочитавшего сообщение';
CREATE TABLE IF NOT EXISTS messaging.chat_room_info
(
    chat_id uuid NOT NULL,
    chat_name character varying(100) NOT NULL,
    info text,
    CONSTRAINT chat_room_info_pk PRIMARY KEY (chat_id)
);
COMMENT ON TABLE messaging.chat_room_info IS 'Данные чата-канала созданного для переписки более 2х человек одновременно. Также в чате может находиться и всего 1 человек.';
COMMENT ON COLUMN messaging.chat_room_info.info IS 'Информация о чате-канале';
CREATE TABLE IF NOT EXISTS messaging.chat_room_messaging
(
    message_id uuid NOT NULL,
    chat_id uuid NOT NULL,
    writer_id uuid NOT NULL,
    message text,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chat_room_messaging_pk PRIMARY KEY (message_id),
    CONSTRAINT writer_id_fk FOREIGN KEY (writer_id)
        REFERENCES messaging.person (person_id),
    CONSTRAINT chat_id_fk FOREIGN KEY (chat_id)
        REFERENCES messaging.chat_room_info (chat_id)
);
COMMENT ON TABLE messaging.chat_room_messaging IS 'Сводная таблица для чатов-каналов (1,2, более 2х участников)';
COMMENT ON COLUMN messaging.chat_room_messaging.writer_id IS 'Информация о пользователя написавшем сообщение в канал';
CREATE TABLE IF NOT EXISTS messaging.chat_room_members
(
    id uuid NOT NULL,
    chat_id uuid NOT NULL,
    member_id uuid NOT NULL,
    CONSTRAINT chat_room_members_pk PRIMARY KEY (id),
    CONSTRAINT chat_id_fk FOREIGN KEY (chat_id)
        REFERENCES messaging.chat_room_info (chat_id),
    CONSTRAINT member_id_fk FOREIGN KEY (member_id)
        REFERENCES messaging.person (person_id)
);
COMMENT ON TABLE messaging.chat_room_members IS 'Таблица-отношение (many to many) между пользователями и чатам-каналам, в которых они состоят';
COMMENT ON COLUMN messaging.chat_room_members.member_id IS 'Индентификатор пользователя, который состоит в соответствующем канале';
--GRANT ALL ON ALL TABLES IN SCHEMA messaging TO test;
--GRANT USAGE ON SCHEMA messaging TO test;