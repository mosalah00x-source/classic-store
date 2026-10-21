-- Generic PL/pgSQL trigger function to update updated_at column
CREATE OR REPLACE FUNCTION store.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach trigger to every table in schema 'store' that has an 'updated_at' column
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT table_name
        FROM information_schema.columns
        WHERE table_schema = 'store'
          AND column_name = 'updated_at'
    LOOP
        EXECUTE format('
            DROP TRIGGER IF EXISTS trg_set_updated_at ON store.%I;
            CREATE TRIGGER trg_set_updated_at
            BEFORE UPDATE ON store.%I
            FOR EACH ROW
            EXECUTE FUNCTION store.set_updated_at();',
            r.table_name, r.table_name);
    END LOOP;
END $$;

/*
Explicit CREATE TRIGGER statements for store schema tables:
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.users FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.addresses FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.categories FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.products FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.warehouses FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.inventory_items FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.carts FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.cart_items FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.orders FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.payments FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.fulfillments FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.shipments FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON store.returns FOR EACH ROW EXECUTE FUNCTION store.set_updated_at();
*/
