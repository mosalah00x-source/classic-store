BEGIN;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE SCHEMA IF NOT EXISTS store;
CREATE TYPE store.user_role AS ENUM ('CUSTOMER','ADMIN','OPERATIONS','WAREHOUSE','SUPPORT');
CREATE TYPE store.product_status AS ENUM ('DRAFT','ACTIVE','ARCHIVED');
CREATE TYPE store.order_status AS ENUM ('PENDING','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED','RETURNED');
CREATE TYPE store.payment_status AS ENUM ('PENDING','AUTHORIZED','PAID','FAILED','REFUNDED','PARTIALLY_REFUNDED','CANCELLED');
CREATE TYPE store.payment_method AS ENUM ('COD','CARD','WALLET','OTHER');
CREATE TYPE store.fulfillment_status AS ENUM ('PENDING','ALLOCATED','PICKING','PACKED','DISPATCHED','DELIVERED','CANCELLED');
CREATE TYPE store.reservation_status AS ENUM ('ACTIVE','CONSUMED','RELEASED','EXPIRED');
CREATE TYPE store.stock_movement_type AS ENUM ('RECEIPT','ADJUSTMENT','RESERVE','RELEASE','SALE','RETURN','TRANSFER_IN','TRANSFER_OUT');
CREATE TABLE store.countries (code char(2) PRIMARY KEY, name_ar text NOT NULL, name_en text NOT NULL, is_enabled boolean NOT NULL DEFAULT false);
INSERT INTO store.countries VALUES ('EG','مصر','Egypt',true);
CREATE TABLE store.governorates (id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, country_code char(2) NOT NULL REFERENCES store.countries(code), code varchar(24) NOT NULL, name_ar text NOT NULL, name_en text, UNIQUE(country_code,code));
CREATE TABLE store.cities (id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, governorate_id bigint NOT NULL REFERENCES store.governorates(id), name_ar text NOT NULL, name_en text);
CREATE TABLE store.users (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), email citext UNIQUE, phone varchar(24) UNIQUE, password_hash text, first_name text, last_name text, role store.user_role NOT NULL DEFAULT 'CUSTOMER', is_active boolean NOT NULL DEFAULT true, created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), CHECK(email IS NOT NULL OR phone IS NOT NULL));
CREATE TABLE store.addresses (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), user_id uuid NOT NULL REFERENCES store.users(id) ON DELETE CASCADE, recipient_name text NOT NULL, phone varchar(24) NOT NULL, country_code char(2) NOT NULL DEFAULT 'EG' REFERENCES store.countries(code), governorate_id bigint REFERENCES store.governorates(id), city_id bigint REFERENCES store.cities(id), district text, street text NOT NULL, building text, apartment text, postal_code text, latitude numeric(9,6), longitude numeric(9,6), is_default boolean NOT NULL DEFAULT false, created_at timestamptz NOT NULL DEFAULT now(), CHECK(latitude BETWEEN -90 AND 90), CHECK(longitude BETWEEN -180 AND 180), CHECK((latitude IS NULL) = (longitude IS NULL)));
CREATE UNIQUE INDEX ux_addresses_default ON store.addresses(user_id) WHERE is_default;
CREATE TABLE store.settings (key text PRIMARY KEY, value jsonb NOT NULL, updated_at timestamptz NOT NULL DEFAULT now());
INSERT INTO store.settings(key,value) VALUES ('currency', '"EGP"'::jsonb),('reservation_minutes','15'::jsonb);
COMMIT;
BEGIN;
CREATE TABLE store.brands (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), name text NOT NULL, slug text NOT NULL UNIQUE, is_active boolean NOT NULL DEFAULT true);
CREATE TABLE store.categories (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), parent_id uuid REFERENCES store.categories(id) ON DELETE RESTRICT, name_ar text NOT NULL, name_en text, slug text NOT NULL UNIQUE, sort_order integer NOT NULL DEFAULT 0, is_active boolean NOT NULL DEFAULT true, CHECK(parent_id IS DISTINCT FROM id));
CREATE TABLE store.products (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), brand_id uuid REFERENCES store.brands(id), name_ar text NOT NULL, name_en text, slug text NOT NULL UNIQUE, description_ar text, description_en text, status store.product_status NOT NULL DEFAULT 'DRAFT', is_physical boolean NOT NULL DEFAULT true CHECK(is_physical), created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now());
CREATE TABLE store.product_categories (product_id uuid NOT NULL REFERENCES store.products(id) ON DELETE CASCADE, category_id uuid NOT NULL REFERENCES store.categories(id) ON DELETE CASCADE, PRIMARY KEY(product_id,category_id));
CREATE TABLE store.product_variants (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), product_id uuid NOT NULL REFERENCES store.products(id) ON DELETE RESTRICT, sku text NOT NULL UNIQUE, barcode text UNIQUE, title_ar text, attributes jsonb NOT NULL DEFAULT '{}'::jsonb, price numeric(14,2) NOT NULL CHECK(price >= 0), compare_at_price numeric(14,2) CHECK(compare_at_price >= 0), cost numeric(14,2) CHECK(cost >= 0), weight_grams integer NOT NULL DEFAULT 0 CHECK(weight_grams >= 0), length_mm integer CHECK(length_mm > 0), width_mm integer CHECK(width_mm > 0), height_mm integer CHECK(height_mm > 0), is_active boolean NOT NULL DEFAULT true, created_at timestamptz NOT NULL DEFAULT now(), UNIQUE(id,product_id));
CREATE TABLE store.product_images (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), product_id uuid NOT NULL REFERENCES store.products(id) ON DELETE CASCADE, variant_id uuid, image_url text NOT NULL, alt_ar text, sort_order integer NOT NULL DEFAULT 0, FOREIGN KEY(variant_id,product_id) REFERENCES store.product_variants(id,product_id));
CREATE TABLE store.coupons (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), code citext NOT NULL UNIQUE, discount_type text NOT NULL CHECK(discount_type IN ('PERCENT','FIXED')), discount_value numeric(14,2) NOT NULL CHECK(discount_value > 0), min_order_amount numeric(14,2) NOT NULL DEFAULT 0 CHECK(min_order_amount >= 0), max_discount numeric(14,2) CHECK(max_discount > 0), starts_at timestamptz, ends_at timestamptz, usage_limit integer CHECK(usage_limit > 0), used_count integer NOT NULL DEFAULT 0 CHECK(used_count >= 0), is_active boolean NOT NULL DEFAULT true, CHECK(discount_type <> 'PERCENT' OR discount_value <= 100), CHECK(ends_at IS NULL OR starts_at IS NULL OR ends_at > starts_at));
CREATE INDEX ix_products_status ON store.products(status);
CREATE INDEX ix_variants_product ON store.product_variants(product_id);
CREATE INDEX ix_categories_parent ON store.categories(parent_id);
COMMIT;
BEGIN;
CREATE TABLE store.warehouses (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), code text NOT NULL UNIQUE, name text NOT NULL, country_code char(2) NOT NULL DEFAULT 'EG' REFERENCES store.countries(code), governorate_id bigint REFERENCES store.governorates(id), city_id bigint REFERENCES store.cities(id), address_line text NOT NULL, latitude numeric(9,6) NOT NULL CHECK(latitude BETWEEN -90 AND 90), longitude numeric(9,6) NOT NULL CHECK(longitude BETWEEN -180 AND 180), priority integer NOT NULL DEFAULT 100, is_active boolean NOT NULL DEFAULT true, created_at timestamptz NOT NULL DEFAULT now());
CREATE TABLE store.inventory (warehouse_id uuid NOT NULL REFERENCES store.warehouses(id), variant_id uuid NOT NULL REFERENCES store.product_variants(id), on_hand integer NOT NULL DEFAULT 0 CHECK(on_hand >= 0), reserved integer NOT NULL DEFAULT 0 CHECK(reserved >= 0), reorder_point integer NOT NULL DEFAULT 0 CHECK(reorder_point >= 0), updated_at timestamptz NOT NULL DEFAULT now(), PRIMARY KEY(warehouse_id,variant_id), CHECK(reserved <= on_hand));
CREATE TABLE store.inventory_movements (id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, warehouse_id uuid NOT NULL REFERENCES store.warehouses(id), variant_id uuid NOT NULL REFERENCES store.product_variants(id), movement_type store.stock_movement_type NOT NULL, quantity_delta integer NOT NULL, reserved_delta integer NOT NULL DEFAULT 0, reference_type text, reference_id uuid, note text, actor_user_id uuid REFERENCES store.users(id), created_at timestamptz NOT NULL DEFAULT now(), CHECK(quantity_delta <> 0 OR reserved_delta <> 0));
CREATE INDEX ix_inventory_variant ON store.inventory(variant_id);
CREATE INDEX ix_inventory_movements_lookup ON store.inventory_movements(warehouse_id,variant_id,created_at DESC);
COMMIT;
BEGIN;
CREATE TABLE store.carts (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), user_id uuid REFERENCES store.users(id) ON DELETE SET NULL, guest_token_hash text UNIQUE, currency char(3) NOT NULL DEFAULT 'EGP', expires_at timestamptz NOT NULL DEFAULT (now() + interval '30 days'), created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), CHECK(user_id IS NOT NULL OR guest_token_hash IS NOT NULL));
CREATE TABLE store.cart_items (cart_id uuid NOT NULL REFERENCES store.carts(id) ON DELETE CASCADE, variant_id uuid NOT NULL REFERENCES store.product_variants(id), quantity integer NOT NULL CHECK(quantity > 0), PRIMARY KEY(cart_id,variant_id));
CREATE TABLE store.orders (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), order_number bigint GENERATED ALWAYS AS IDENTITY UNIQUE, user_id uuid REFERENCES store.users(id) ON DELETE SET NULL, guest_email citext, guest_phone varchar(24), customer_name text NOT NULL, customer_email citext, customer_phone varchar(24) NOT NULL, status store.order_status NOT NULL DEFAULT 'PENDING', payment_status store.payment_status NOT NULL DEFAULT 'PENDING', currency char(3) NOT NULL DEFAULT 'EGP', subtotal numeric(14,2) NOT NULL CHECK(subtotal >= 0), discount_total numeric(14,2) NOT NULL DEFAULT 0 CHECK(discount_total >= 0), shipping_total numeric(14,2) NOT NULL DEFAULT 0 CHECK(shipping_total >= 0), tax_total numeric(14,2) NOT NULL DEFAULT 0 CHECK(tax_total >= 0), grand_total numeric(14,2) GENERATED ALWAYS AS (subtotal-discount_total+shipping_total+tax_total) STORED, coupon_id uuid REFERENCES store.coupons(id), shipping_address jsonb NOT NULL, billing_address jsonb, shipping_latitude numeric(9,6), shipping_longitude numeric(9,6), notes text, placed_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), CHECK(user_id IS NOT NULL OR guest_phone IS NOT NULL OR guest_email IS NOT NULL), CHECK(discount_total <= subtotal), CHECK(shipping_latitude BETWEEN -90 AND 90), CHECK(shipping_longitude BETWEEN -180 AND 180), CHECK((shipping_latitude IS NULL) = (shipping_longitude IS NULL)));
CREATE TABLE store.order_items (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), order_id uuid NOT NULL REFERENCES store.orders(id) ON DELETE RESTRICT, variant_id uuid NOT NULL REFERENCES store.product_variants(id), sku_snapshot text NOT NULL, product_name_snapshot text NOT NULL, variant_snapshot jsonb NOT NULL DEFAULT '{}'::jsonb, quantity integer NOT NULL CHECK(quantity > 0), unit_price numeric(14,2) NOT NULL CHECK(unit_price >= 0), discount_amount numeric(14,2) NOT NULL DEFAULT 0 CHECK(discount_amount >= 0), line_total numeric(14,2) GENERATED ALWAYS AS (quantity*unit_price-discount_amount) STORED, CHECK(discount_amount <= quantity*unit_price), UNIQUE(order_id,variant_id));
CREATE TABLE store.order_status_history (id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, order_id uuid NOT NULL REFERENCES store.orders(id), old_status store.order_status, new_status store.order_status NOT NULL, changed_by uuid REFERENCES store.users(id), note text, created_at timestamptz NOT NULL DEFAULT now());
CREATE TABLE store.fulfillments (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), order_id uuid NOT NULL REFERENCES store.orders(id), warehouse_id uuid NOT NULL REFERENCES store.warehouses(id), status store.fulfillment_status NOT NULL DEFAULT 'PENDING', shipping_provider text, tracking_number text, shipped_at timestamptz, delivered_at timestamptz, created_at timestamptz NOT NULL DEFAULT now(), UNIQUE(order_id,warehouse_id));
CREATE TABLE store.fulfillment_items (fulfillment_id uuid NOT NULL REFERENCES store.fulfillments(id), order_item_id uuid NOT NULL REFERENCES store.order_items(id), quantity integer NOT NULL CHECK(quantity > 0), PRIMARY KEY(fulfillment_id,order_item_id));
CREATE TABLE store.inventory_reservations (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), order_id uuid NOT NULL REFERENCES store.orders(id), order_item_id uuid NOT NULL REFERENCES store.order_items(id), warehouse_id uuid NOT NULL REFERENCES store.warehouses(id), variant_id uuid NOT NULL REFERENCES store.product_variants(id), quantity integer NOT NULL CHECK(quantity > 0), status store.reservation_status NOT NULL DEFAULT 'ACTIVE', expires_at timestamptz NOT NULL, created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), UNIQUE(order_item_id,warehouse_id));
CREATE INDEX ix_orders_user_date ON store.orders(user_id,placed_at DESC);
CREATE INDEX ix_orders_status_date ON store.orders(status,placed_at DESC);
CREATE INDEX ix_reservations_expiry ON store.inventory_reservations(expires_at) WHERE status='ACTIVE';
COMMIT;
BEGIN;
CREATE TABLE store.payments (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), order_id uuid NOT NULL REFERENCES store.orders(id), method store.payment_method NOT NULL, status store.payment_status NOT NULL DEFAULT 'PENDING', provider text, provider_payment_id text, amount numeric(14,2) NOT NULL CHECK(amount >= 0), currency char(3) NOT NULL DEFAULT 'EGP', idempotency_key text NOT NULL UNIQUE, paid_at timestamptz, created_at timestamptz NOT NULL DEFAULT now(), UNIQUE(provider,provider_payment_id));
CREATE TABLE store.payment_events (id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, payment_id uuid NOT NULL REFERENCES store.payments(id), provider_event_id text, event_type text NOT NULL, payload jsonb NOT NULL, received_at timestamptz NOT NULL DEFAULT now(), UNIQUE(payment_id,provider_event_id));
CREATE TABLE store.refunds (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), payment_id uuid NOT NULL REFERENCES store.payments(id), amount numeric(14,2) NOT NULL CHECK(amount > 0), reason text, provider_refund_id text UNIQUE, status text NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','SUCCEEDED','FAILED')), created_at timestamptz NOT NULL DEFAULT now());
CREATE TABLE store.shipping_zones (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), name text NOT NULL, country_code char(2) NOT NULL REFERENCES store.countries(code), governorate_id bigint REFERENCES store.governorates(id), is_active boolean NOT NULL DEFAULT true);
CREATE TABLE store.shipping_rates (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), zone_id uuid NOT NULL REFERENCES store.shipping_zones(id), min_weight_grams integer NOT NULL DEFAULT 0 CHECK(min_weight_grams >= 0), max_weight_grams integer CHECK(max_weight_grams > 0), amount numeric(14,2) NOT NULL CHECK(amount >= 0), currency char(3) NOT NULL DEFAULT 'EGP', CHECK(max_weight_grams IS NULL OR max_weight_grams > min_weight_grams));
CREATE TABLE store.returns (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), order_id uuid NOT NULL REFERENCES store.orders(id), status text NOT NULL DEFAULT 'REQUESTED' CHECK(status IN ('REQUESTED','APPROVED','REJECTED','RECEIVED','REFUNDED')), reason text, created_at timestamptz NOT NULL DEFAULT now());
CREATE TABLE store.return_items (return_id uuid NOT NULL REFERENCES store.returns(id), order_item_id uuid NOT NULL REFERENCES store.order_items(id), quantity integer NOT NULL CHECK(quantity > 0), PRIMARY KEY(return_id,order_item_id));
CREATE TABLE store.outbox_events (id uuid PRIMARY KEY DEFAULT gen_random_uuid(), aggregate_type text NOT NULL, aggregate_id uuid NOT NULL, event_type text NOT NULL, payload jsonb NOT NULL, created_at timestamptz NOT NULL DEFAULT now(), published_at timestamptz, attempts integer NOT NULL DEFAULT 0);
CREATE INDEX ix_outbox_unpublished ON store.outbox_events(created_at) WHERE published_at IS NULL;
CREATE INDEX ix_payments_order ON store.payments(order_id);
COMMIT;
BEGIN;
-- Approximate great-circle distance (km). Use road-network ETA in application if required.
CREATE FUNCTION store.distance_km(lat1 numeric, lon1 numeric, lat2 numeric, lon2 numeric)
RETURNS double precision LANGUAGE sql IMMUTABLE STRICT PARALLEL SAFE AS $$
 SELECT 6371.0088 * 2 * asin(least(1.0,sqrt(
   power(sin(radians((lat2-lat1)::double precision)/2),2)
   + cos(radians(lat1::double precision))*cos(radians(lat2::double precision))
   * power(sin(radians((lon2-lon1)::double precision)/2),2))))
$$;
-- Read-only candidate ranking: one warehouse must have all lines in stock.
CREATE FUNCTION store.rank_warehouses(p_order_id uuid)
RETURNS TABLE(warehouse_id uuid, distance_km double precision, warehouse_priority integer)
LANGUAGE sql STABLE AS $$
 SELECT w.id,store.distance_km(o.shipping_latitude,o.shipping_longitude,w.latitude,w.longitude),w.priority
 FROM store.orders o CROSS JOIN store.warehouses w
 WHERE o.id=p_order_id AND o.shipping_latitude IS NOT NULL AND w.is_active
 AND w.country_code = COALESCE(o.shipping_address->>'country_code','EG')
 AND EXISTS (SELECT 1 FROM store.order_items oi WHERE oi.order_id=o.id)
 AND NOT EXISTS (
   SELECT 1 FROM store.order_items oi
   LEFT JOIN store.inventory i ON i.variant_id=oi.variant_id AND i.warehouse_id=w.id
   WHERE oi.order_id=o.id AND COALESCE(i.on_hand-i.reserved,0)<oi.quantity)
 ORDER BY 2,w.priority,w.id
$$;
-- Serializes allocation per order and globally per warehouse inventory by row locks.
-- Returns NULL when no single warehouse can cover all order lines.
CREATE FUNCTION store.reserve_order_at_nearest_warehouse(p_order_id uuid, p_ttl_minutes integer DEFAULT 15)
RETURNS uuid LANGUAGE plpgsql AS $$
DECLARE v_warehouse uuid; v_candidate record; v_item record; v_order store.orders%ROWTYPE; v_ok boolean;
BEGIN
 IF p_ttl_minutes NOT BETWEEN 1 AND 1440 THEN RAISE EXCEPTION 'Invalid TTL'; END IF;
 SELECT * INTO v_order FROM store.orders WHERE id=p_order_id FOR UPDATE;
 IF NOT FOUND THEN RAISE EXCEPTION 'Order not found'; END IF;
 IF v_order.status <> 'PENDING' THEN RAISE EXCEPTION 'Order must be PENDING'; END IF;
 IF v_order.shipping_latitude IS NULL THEN RAISE EXCEPTION 'Shipping coordinates required'; END IF;
 IF EXISTS(SELECT 1 FROM store.inventory_reservations WHERE order_id=p_order_id AND status='ACTIVE') THEN
   RAISE EXCEPTION 'Order already reserved'; END IF;
 FOR v_candidate IN SELECT r.warehouse_id FROM store.rank_warehouses(p_order_id) r LOOP
   v_ok := true;
   -- Lock rows in consistent variant order, avoiding overselling under concurrency.
   FOR v_item IN SELECT oi.id AS item_id,oi.variant_id,oi.quantity FROM store.order_items oi
                 WHERE oi.order_id=p_order_id ORDER BY oi.variant_id LOOP
     PERFORM 1 FROM store.inventory i WHERE i.warehouse_id=v_candidate.warehouse_id
       AND i.variant_id=v_item.variant_id FOR UPDATE;
     IF NOT FOUND OR NOT EXISTS (
       SELECT 1 FROM store.inventory i WHERE i.warehouse_id=v_candidate.warehouse_id
       AND i.variant_id=v_item.variant_id AND i.on_hand-i.reserved >= v_item.quantity) THEN
       v_ok:=false; EXIT;
     END IF;
   END LOOP;
   IF v_ok THEN v_warehouse:=v_candidate.warehouse_id; EXIT; END IF;
 END LOOP;
 IF v_warehouse IS NULL THEN RETURN NULL; END IF;
 FOR v_item IN SELECT oi.id AS item_id,oi.variant_id,oi.quantity FROM store.order_items oi
               WHERE oi.order_id=p_order_id ORDER BY oi.variant_id LOOP
   UPDATE store.inventory SET reserved=reserved+v_item.quantity,updated_at=now()
   WHERE warehouse_id=v_warehouse AND variant_id=v_item.variant_id;
   INSERT INTO store.inventory_reservations(order_id,order_item_id,warehouse_id,variant_id,quantity,expires_at)
   VALUES(p_order_id,v_item.item_id,v_warehouse,v_item.variant_id,v_item.quantity,now()+make_interval(mins=>p_ttl_minutes));
   INSERT INTO store.inventory_movements(warehouse_id,variant_id,movement_type,quantity_delta,reserved_delta,reference_type,reference_id)
   VALUES(v_warehouse,v_item.variant_id,'RESERVE',0,v_item.quantity,'ORDER',p_order_id);
 END LOOP;
 INSERT INTO store.fulfillments(order_id,warehouse_id,status) VALUES(p_order_id,v_warehouse,'ALLOCATED')
 ON CONFLICT(order_id,warehouse_id) DO NOTHING;
 INSERT INTO store.fulfillment_items(fulfillment_id,order_item_id,quantity)
 SELECT f.id,oi.id,oi.quantity FROM store.fulfillments f JOIN store.order_items oi ON oi.order_id=f.order_id
 WHERE f.order_id=p_order_id AND f.warehouse_id=v_warehouse ON CONFLICT DO NOTHING;
 RETURN v_warehouse;
END $$;
CREATE FUNCTION store.release_order_reservations(p_order_id uuid)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE r record; n integer:=0;
BEGIN
 PERFORM 1 FROM store.orders WHERE id=p_order_id FOR UPDATE;
 FOR r IN SELECT * FROM store.inventory_reservations WHERE order_id=p_order_id AND status='ACTIVE'
          ORDER BY variant_id FOR UPDATE LOOP
   UPDATE store.inventory SET reserved=reserved-r.quantity,updated_at=now()
   WHERE warehouse_id=r.warehouse_id AND variant_id=r.variant_id;
   UPDATE store.inventory_reservations SET status='RELEASED',updated_at=now() WHERE id=r.id;
   INSERT INTO store.inventory_movements(warehouse_id,variant_id,movement_type,quantity_delta,reserved_delta,reference_type,reference_id)
   VALUES(r.warehouse_id,r.variant_id,'RELEASE',0,-r.quantity,'ORDER',p_order_id);
   n:=n+1;
 END LOOP;
 RETURN n;
END $$;
CREATE FUNCTION store.consume_order_reservations(p_order_id uuid)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE r record; n integer:=0;
BEGIN
 PERFORM 1 FROM store.orders WHERE id=p_order_id FOR UPDATE;
 FOR r IN SELECT * FROM store.inventory_reservations WHERE order_id=p_order_id AND status='ACTIVE'
          ORDER BY variant_id FOR UPDATE LOOP
   UPDATE store.inventory SET reserved=reserved-r.quantity,on_hand=on_hand-r.quantity,updated_at=now()
   WHERE warehouse_id=r.warehouse_id AND variant_id=r.variant_id;
   UPDATE store.inventory_reservations SET status='CONSUMED',updated_at=now() WHERE id=r.id;
   INSERT INTO store.inventory_movements(warehouse_id,variant_id,movement_type,quantity_delta,reserved_delta,reference_type,reference_id)
   VALUES(r.warehouse_id,r.variant_id,'SALE',-r.quantity,-r.quantity,'ORDER',p_order_id);
   n:=n+1;
 END LOOP;
 RETURN n;
END $$;
COMMIT;
