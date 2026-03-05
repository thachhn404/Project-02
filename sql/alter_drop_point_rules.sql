IF EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'fk_point_transactions_rule'
)
BEGIN
    ALTER TABLE point_transactions DROP CONSTRAINT fk_point_transactions_rule;
END

IF COL_LENGTH('point_transactions', 'rule_id') IS NOT NULL
BEGIN
    ALTER TABLE point_transactions DROP COLUMN rule_id;
END

IF OBJECT_ID('point_rules', 'U') IS NOT NULL
BEGIN
    DROP TABLE point_rules;
END

