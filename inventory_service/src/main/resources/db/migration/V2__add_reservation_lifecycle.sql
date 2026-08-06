ALTER TABLE stock_reservations
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;

ALTER TABLE stock_reservations
    ADD COLUMN IF NOT EXISTS committed_at TIMESTAMP;

UPDATE stock_reservations
SET expires_at = DATEADD('MINUTE', 10, created_at)
WHERE status = 'RESERVED'
  AND expires_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reservation_status_expires_at
    ON stock_reservations(status, expires_at);
