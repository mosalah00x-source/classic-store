# قاموس بيانات Classic Store — Data Dictionary

المصدر: `classic_store_all.sql` — جميع الجداول ضمن schema `store`.

| الجدول (Table) | العمود (English) | العمود (العربية) |
|---|---|---|
| `store.countries` | `code` | الرمز |
| `store.countries` | `name_ar` | الاسم بالعربية |
| `store.countries` | `name_en` | الاسم بالإنجليزية |
| `store.countries` | `is_enabled` | مفعّل |
| `store.governorates` | `id` | المعرّف |
| `store.governorates` | `country_code` | رمز الدولة |
| `store.governorates` | `code` | الرمز |
| `store.governorates` | `name_ar` | الاسم بالعربية |
| `store.governorates` | `name_en` | الاسم بالإنجليزية |
| `store.cities` | `id` | المعرّف |
| `store.cities` | `governorate_id` | معرّف المحافظة |
| `store.cities` | `name_ar` | الاسم بالعربية |
| `store.cities` | `name_en` | الاسم بالإنجليزية |
| `store.users` | `id` | المعرّف |
| `store.users` | `email` | البريد الإلكتروني |
| `store.users` | `phone` | رقم الهاتف |
| `store.users` | `password_hash` | تجزئة كلمة المرور |
| `store.users` | `first_name` | الاسم الأول |
| `store.users` | `last_name` | اسم العائلة |
| `store.users` | `role` | الدور |
| `store.users` | `is_active` | نشط |
| `store.users` | `created_at` | تاريخ الإنشاء |
| `store.users` | `updated_at` | تاريخ التحديث |
| `store.addresses` | `id` | المعرّف |
| `store.addresses` | `user_id` | معرّف المستخدم |
| `store.addresses` | `recipient_name` | اسم المستلم |
| `store.addresses` | `phone` | رقم الهاتف |
| `store.addresses` | `country_code` | رمز الدولة |
| `store.addresses` | `governorate_id` | معرّف المحافظة |
| `store.addresses` | `city_id` | معرّف المدينة |
| `store.addresses` | `district` | الحي |
| `store.addresses` | `street` | الشارع |
| `store.addresses` | `building` | المبنى |
| `store.addresses` | `apartment` | الشقة |
| `store.addresses` | `postal_code` | الرمز البريدي |
| `store.addresses` | `latitude` | خط العرض |
| `store.addresses` | `longitude` | خط الطول |
| `store.addresses` | `is_default` | العنوان الافتراضي |
| `store.addresses` | `created_at` | تاريخ الإنشاء |
| `store.settings` | `key` | المفتاح |
| `store.settings` | `value` | القيمة |
| `store.settings` | `updated_at` | تاريخ التحديث |
| `store.brands` | `id` | المعرّف |
| `store.brands` | `name` | الاسم |
| `store.brands` | `slug` | المعرّف النصي للرابط |
| `store.brands` | `is_active` | نشط |
| `store.categories` | `id` | المعرّف |
| `store.categories` | `parent_id` | معرّف التصنيف الأب |
| `store.categories` | `name_ar` | الاسم بالعربية |
| `store.categories` | `name_en` | الاسم بالإنجليزية |
| `store.categories` | `slug` | المعرّف النصي للرابط |
| `store.categories` | `sort_order` | ترتيب العرض |
| `store.categories` | `is_active` | نشط |
| `store.products` | `id` | المعرّف |
| `store.products` | `brand_id` | معرّف العلامة التجارية |
| `store.products` | `name_ar` | الاسم بالعربية |
| `store.products` | `name_en` | الاسم بالإنجليزية |
| `store.products` | `slug` | المعرّف النصي للرابط |
| `store.products` | `description_ar` | الوصف بالعربية |
| `store.products` | `description_en` | الوصف بالإنجليزية |
| `store.products` | `status` | الحالة |
| `store.products` | `is_physical` | منتج مادي |
| `store.products` | `created_at` | تاريخ الإنشاء |
| `store.products` | `updated_at` | تاريخ التحديث |
| `store.product_categories` | `product_id` | معرّف المنتج |
| `store.product_categories` | `category_id` | معرّف التصنيف |
| `store.product_variants` | `id` | المعرّف |
| `store.product_variants` | `product_id` | معرّف المنتج |
| `store.product_variants` | `sku` | رمز وحدة المخزون |
| `store.product_variants` | `barcode` | الباركود |
| `store.product_variants` | `title_ar` | عنوان المتغير بالعربية |
| `store.product_variants` | `attributes` | الخصائص |
| `store.product_variants` | `price` | السعر |
| `store.product_variants` | `compare_at_price` | السعر قبل الخصم |
| `store.product_variants` | `cost` | التكلفة |
| `store.product_variants` | `weight_grams` | الوزن بالجرام |
| `store.product_variants` | `length_mm` | الطول بالمليمتر |
| `store.product_variants` | `width_mm` | العرض بالمليمتر |
| `store.product_variants` | `height_mm` | الارتفاع بالمليمتر |
| `store.product_variants` | `is_active` | نشط |
| `store.product_variants` | `created_at` | تاريخ الإنشاء |
| `store.product_images` | `id` | المعرّف |
| `store.product_images` | `product_id` | معرّف المنتج |
| `store.product_images` | `variant_id` | معرّف متغير المنتج |
| `store.product_images` | `image_url` | رابط الصورة |
| `store.product_images` | `alt_ar` | النص البديل بالعربية |
| `store.product_images` | `sort_order` | ترتيب العرض |
| `store.coupons` | `id` | المعرّف |
| `store.coupons` | `code` | الرمز |
| `store.coupons` | `discount_type` | نوع الخصم |
| `store.coupons` | `discount_value` | قيمة الخصم |
| `store.coupons` | `min_order_amount` | الحد الأدنى للطلب |
| `store.coupons` | `max_discount` | الحد الأقصى للخصم |
| `store.coupons` | `starts_at` | بداية الصلاحية |
| `store.coupons` | `ends_at` | نهاية الصلاحية |
| `store.coupons` | `usage_limit` | حد الاستخدام |
| `store.coupons` | `used_count` | عدد مرات الاستخدام |
| `store.coupons` | `is_active` | نشط |
| `store.warehouses` | `id` | المعرّف |
| `store.warehouses` | `code` | الرمز |
| `store.warehouses` | `name` | الاسم |
| `store.warehouses` | `country_code` | رمز الدولة |
| `store.warehouses` | `governorate_id` | معرّف المحافظة |
| `store.warehouses` | `city_id` | معرّف المدينة |
| `store.warehouses` | `address_line` | عنوان المخزن |
| `store.warehouses` | `latitude` | خط العرض |
| `store.warehouses` | `longitude` | خط الطول |
| `store.warehouses` | `priority` | أولوية المخزن |
| `store.warehouses` | `is_active` | نشط |
| `store.warehouses` | `created_at` | تاريخ الإنشاء |
| `store.inventory` | `warehouse_id` | معرّف المخزن |
| `store.inventory` | `variant_id` | معرّف متغير المنتج |
| `store.inventory` | `on_hand` | الكمية الفعلية |
| `store.inventory` | `reserved` | الكمية المحجوزة |
| `store.inventory` | `reorder_point` | حد إعادة الطلب |
| `store.inventory` | `updated_at` | تاريخ التحديث |
| `store.inventory_movements` | `id` | المعرّف |
| `store.inventory_movements` | `warehouse_id` | معرّف المخزن |
| `store.inventory_movements` | `variant_id` | معرّف متغير المنتج |
| `store.inventory_movements` | `movement_type` | نوع حركة المخزون |
| `store.inventory_movements` | `quantity_delta` | تغيّر الكمية الفعلية |
| `store.inventory_movements` | `reserved_delta` | تغيّر الكمية المحجوزة |
| `store.inventory_movements` | `reference_type` | نوع المرجع |
| `store.inventory_movements` | `reference_id` | معرّف المرجع |
| `store.inventory_movements` | `note` | ملاحظة |
| `store.inventory_movements` | `actor_user_id` | معرّف المستخدم المنفّذ |
| `store.inventory_movements` | `created_at` | تاريخ الإنشاء |
| `store.carts` | `id` | المعرّف |
| `store.carts` | `user_id` | معرّف المستخدم |
| `store.carts` | `guest_token_hash` | تجزئة رمز الزائر |
| `store.carts` | `currency` | العملة |
| `store.carts` | `expires_at` | تاريخ انتهاء الصلاحية |
| `store.carts` | `created_at` | تاريخ الإنشاء |
| `store.carts` | `updated_at` | تاريخ التحديث |
| `store.cart_items` | `cart_id` | معرّف السلة |
| `store.cart_items` | `variant_id` | معرّف متغير المنتج |
| `store.cart_items` | `quantity` | الكمية |
| `store.orders` | `id` | المعرّف |
| `store.orders` | `order_number` | رقم الطلب |
| `store.orders` | `user_id` | معرّف المستخدم |
| `store.orders` | `guest_email` | بريد الزائر |
| `store.orders` | `guest_phone` | هاتف الزائر |
| `store.orders` | `customer_name` | اسم العميل |
| `store.orders` | `customer_email` | بريد العميل |
| `store.orders` | `customer_phone` | هاتف العميل |
| `store.orders` | `status` | الحالة |
| `store.orders` | `payment_status` | حالة الدفع |
| `store.orders` | `currency` | العملة |
| `store.orders` | `subtotal` | المجموع الفرعي |
| `store.orders` | `discount_total` | إجمالي الخصومات |
| `store.orders` | `shipping_total` | تكلفة الشحن |
| `store.orders` | `tax_total` | إجمالي الضرائب |
| `store.orders` | `grand_total` | الإجمالي النهائي |
| `store.orders` | `coupon_id` | معرّف الكوبون |
| `store.orders` | `shipping_address` | عنوان الشحن |
| `store.orders` | `billing_address` | عنوان الفوترة |
| `store.orders` | `shipping_latitude` | خط عرض الشحن |
| `store.orders` | `shipping_longitude` | خط طول الشحن |
| `store.orders` | `notes` | ملاحظات |
| `store.orders` | `placed_at` | تاريخ تقديم الطلب |
| `store.orders` | `updated_at` | تاريخ التحديث |
| `store.order_items` | `id` | المعرّف |
| `store.order_items` | `order_id` | معرّف الطلب |
| `store.order_items` | `variant_id` | معرّف متغير المنتج |
| `store.order_items` | `sku_snapshot` | نسخة رمز المخزون |
| `store.order_items` | `product_name_snapshot` | نسخة اسم المنتج |
| `store.order_items` | `variant_snapshot` | نسخة بيانات المتغير |
| `store.order_items` | `quantity` | الكمية |
| `store.order_items` | `unit_price` | سعر الوحدة |
| `store.order_items` | `discount_amount` | مبلغ الخصم |
| `store.order_items` | `line_total` | إجمالي البند |
| `store.order_status_history` | `id` | المعرّف |
| `store.order_status_history` | `order_id` | معرّف الطلب |
| `store.order_status_history` | `old_status` | الحالة السابقة |
| `store.order_status_history` | `new_status` | الحالة الجديدة |
| `store.order_status_history` | `changed_by` | تم التغيير بواسطة |
| `store.order_status_history` | `note` | ملاحظة |
| `store.order_status_history` | `created_at` | تاريخ الإنشاء |
| `store.fulfillments` | `id` | المعرّف |
| `store.fulfillments` | `order_id` | معرّف الطلب |
| `store.fulfillments` | `warehouse_id` | معرّف المخزن |
| `store.fulfillments` | `status` | الحالة |
| `store.fulfillments` | `shipping_provider` | شركة الشحن |
| `store.fulfillments` | `tracking_number` | رقم التتبع |
| `store.fulfillments` | `shipped_at` | تاريخ الشحن |
| `store.fulfillments` | `delivered_at` | تاريخ التسليم |
| `store.fulfillments` | `created_at` | تاريخ الإنشاء |
| `store.fulfillment_items` | `fulfillment_id` | معرّف التجهيز |
| `store.fulfillment_items` | `order_item_id` | معرّف بند الطلب |
| `store.fulfillment_items` | `quantity` | الكمية |
| `store.inventory_reservations` | `id` | المعرّف |
| `store.inventory_reservations` | `order_id` | معرّف الطلب |
| `store.inventory_reservations` | `order_item_id` | معرّف بند الطلب |
| `store.inventory_reservations` | `warehouse_id` | معرّف المخزن |
| `store.inventory_reservations` | `variant_id` | معرّف متغير المنتج |
| `store.inventory_reservations` | `quantity` | الكمية |
| `store.inventory_reservations` | `status` | الحالة |
| `store.inventory_reservations` | `expires_at` | تاريخ انتهاء الصلاحية |
| `store.inventory_reservations` | `created_at` | تاريخ الإنشاء |
| `store.inventory_reservations` | `updated_at` | تاريخ التحديث |
| `store.payments` | `id` | المعرّف |
| `store.payments` | `order_id` | معرّف الطلب |
| `store.payments` | `method` | طريقة الدفع |
| `store.payments` | `status` | الحالة |
| `store.payments` | `provider` | مزود الدفع |
| `store.payments` | `provider_payment_id` | معرّف العملية لدى المزود |
| `store.payments` | `amount` | المبلغ |
| `store.payments` | `currency` | العملة |
| `store.payments` | `idempotency_key` | مفتاح منع التكرار |
| `store.payments` | `paid_at` | تاريخ الدفع |
| `store.payments` | `created_at` | تاريخ الإنشاء |
| `store.payment_events` | `id` | المعرّف |
| `store.payment_events` | `payment_id` | معرّف الدفع |
| `store.payment_events` | `provider_event_id` | معرّف حدث المزود |
| `store.payment_events` | `event_type` | نوع الحدث |
| `store.payment_events` | `payload` | محتوى الحدث |
| `store.payment_events` | `received_at` | تاريخ الاستلام |
| `store.refunds` | `id` | المعرّف |
| `store.refunds` | `payment_id` | معرّف الدفع |
| `store.refunds` | `amount` | المبلغ |
| `store.refunds` | `reason` | السبب |
| `store.refunds` | `provider_refund_id` | معرّف الاسترداد لدى المزود |
| `store.refunds` | `status` | الحالة |
| `store.refunds` | `created_at` | تاريخ الإنشاء |
| `store.shipping_zones` | `id` | المعرّف |
| `store.shipping_zones` | `name` | الاسم |
| `store.shipping_zones` | `country_code` | رمز الدولة |
| `store.shipping_zones` | `governorate_id` | معرّف المحافظة |
| `store.shipping_zones` | `is_active` | نشط |
| `store.shipping_rates` | `id` | المعرّف |
| `store.shipping_rates` | `zone_id` | معرّف منطقة الشحن |
| `store.shipping_rates` | `min_weight_grams` | الحد الأدنى للوزن بالجرام |
| `store.shipping_rates` | `max_weight_grams` | الحد الأقصى للوزن بالجرام |
| `store.shipping_rates` | `amount` | المبلغ |
| `store.shipping_rates` | `currency` | العملة |
| `store.returns` | `id` | المعرّف |
| `store.returns` | `order_id` | معرّف الطلب |
| `store.returns` | `status` | الحالة |
| `store.returns` | `reason` | السبب |
| `store.returns` | `created_at` | تاريخ الإنشاء |
| `store.return_items` | `return_id` | معرّف الإرجاع |
| `store.return_items` | `order_item_id` | معرّف بند الطلب |
| `store.return_items` | `quantity` | الكمية |
| `store.outbox_events` | `id` | المعرّف |
| `store.outbox_events` | `aggregate_type` | نوع الكيان |
| `store.outbox_events` | `aggregate_id` | معرّف الكيان |
| `store.outbox_events` | `event_type` | نوع الحدث |
| `store.outbox_events` | `payload` | محتوى الحدث |
| `store.outbox_events` | `created_at` | تاريخ الإنشاء |
| `store.outbox_events` | `published_at` | تاريخ النشر |
| `store.outbox_events` | `attempts` | عدد المحاولات |

**الإجمالي:** 32 جدولًا، 251 عمودًا.
