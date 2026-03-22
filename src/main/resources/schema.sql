ALTER TABLE IF EXISTS matches ADD COLUMN IF NOT EXISTS status varchar(255);

UPDATE matches
SET status = 'SCHEDULED'
WHERE status IS NULL;

ALTER TABLE IF EXISTS matches
    ALTER COLUMN status SET DEFAULT 'SCHEDULED';

ALTER TABLE IF EXISTS matches
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE IF EXISTS match_expense_discounts
    ADD COLUMN IF NOT EXISTS description varchar(255);

CREATE TABLE IF NOT EXISTS wallet_recharge_requests (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    requested_by_id BIGINT NOT NULL,
    approved_by_id BIGINT,
    amount DOUBLE PRECISION NOT NULL,
    description VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    request_type VARCHAR(50) NOT NULL,
    request_date DATE NOT NULL,
    approved_date DATE
);
