ALTER TABLE subscriptions
    DROP CONSTRAINT uq_subscriptions_name;

CREATE UNIQUE INDEX ux_subscriptions_name_lower
    ON subscriptions (LOWER(name));