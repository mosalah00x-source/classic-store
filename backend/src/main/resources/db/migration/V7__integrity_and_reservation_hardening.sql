CREATE OR REPLACE FUNCTION store.rank_warehouses(
    p_governorate VARCHAR
)
RETURNS TABLE (
    warehouse_id BIGINT,
    warehouse_code VARCHAR,
    priority INT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        w.id AS warehouse_id,
        w.code AS warehouse_code,
        CASE
            WHEN w.governorate = p_governorate THEN 1
            ELSE 2
        END AS priority
    FROM store.warehouses w
    WHERE w.is_active = TRUE
    ORDER BY priority ASC, w.id ASC;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION store.reserve_order_at_nearest_warehouse(
    p_order_id BIGINT,
    p_governorate VARCHAR
)
RETURNS BOOLEAN AS $$
DECLARE
    v_warehouse_id BIGINT;
BEGIN
    FOR v_warehouse_id IN
        SELECT warehouse_id FROM store.rank_warehouses(p_governorate)
    LOOP
        IF NOT EXISTS (
            SELECT 1
            FROM store.order_items oi
            LEFT JOIN store.inventory_items ii
                ON ii.product_id = oi.product_id
               AND ii.warehouse_id = v_warehouse_id
            WHERE oi.order_id = p_order_id
              AND (ii.id IS NULL OR ii.quantity_available - ii.quantity_reserved < oi.quantity)
        ) THEN
            UPDATE store.inventory_items ii
            SET quantity_reserved = ii.quantity_reserved + oi.quantity
            FROM store.order_items oi
            WHERE oi.order_id = p_order_id
              AND ii.product_id = oi.product_id
              AND ii.warehouse_id = v_warehouse_id;

            RETURN TRUE;
        END IF;
    END LOOP;

    RETURN FALSE;
END;
$$ LANGUAGE plpgsql;
