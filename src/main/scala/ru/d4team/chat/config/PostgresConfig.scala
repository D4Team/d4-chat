package ru.d4team.chat.config

final case class PostgresConfig(
    host: String,
    port: Int,
    db: String,
    user: String,
    password: String
)
