CREATE TABLE IF NOT EXISTS oum_profiles
(
    uuid
    VARCHAR
(
    36
) NOT NULL,
    name VARCHAR
(
    32
) NOT NULL,
    created_at BIGINT NOT NULL,
    last_used BIGINT NOT NULL,
    state_json LONGTEXT NOT NULL,
    balance DOUBLE NOT NULL DEFAULT 0,
    primary_group VARCHAR
(
    64
),
    groups_json TEXT,
    PRIMARY KEY
(
    uuid,
    name
),
    INDEX idx_oum_profiles_uuid
(
    uuid
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;