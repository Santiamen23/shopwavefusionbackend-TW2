-- ============================================================
-- Migración: soft delete de productos + limpieza de cart_items huérfanos
-- Ejecutar una sola vez. Hacer BACKUP antes.
-- ============================================================

START TRANSACTION;

-- 1) Agregar columna deleted_at a product (idempotente)
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'product'
    AND column_name = 'deleted_at'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE product ADD COLUMN deleted_at DATETIME(6) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) Diagnóstico: listar cart_items huérfanos ANTES de borrar
SELECT 'HUERFANOS_DETECTADOS' AS seccion, ci.id, ci.product_id, ci.user_id, ci.size
FROM cart_item ci
LEFT JOIN product p ON ci.product_id = p.id
WHERE p.id IS NULL OR p.deleted_at IS NOT NULL;

-- 3) Borrar cart_items cuyo product no existe o está soft-deleted
DELETE ci FROM cart_item ci
LEFT JOIN product p ON ci.product_id = p.id
WHERE p.id IS NULL OR p.deleted_at IS NOT NULL;

COMMIT;

-- 4) Verificación
SELECT
  (SELECT COUNT(*) FROM product WHERE deleted_at IS NULL)  AS productos_activos,
  (SELECT COUNT(*) FROM product WHERE deleted_at IS NOT NULL) AS productos_soft_deleted,
  (SELECT COUNT(*) FROM cart_item ci
     LEFT JOIN product p ON ci.product_id = p.id
     WHERE p.id IS NULL OR p.deleted_at IS NOT NULL) AS cart_items_huerfanos_restantes;
