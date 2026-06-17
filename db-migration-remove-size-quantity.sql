-- ============================================================
-- Migración: eliminar quantity por variante de Size
-- 1) Suma size_quantity por producto y lo guarda en product.quantity
-- 2) Elimina la columna size_quantity de la tabla de la colección
-- Ejecutar una sola vez ANTES de redesplegar el backend nuevo.
-- ============================================================

START TRANSACTION;

-- Diagnóstico: nombres reales de tablas y columnas
SELECT 'TABLAS_RELACIONADAS' AS seccion, TABLE_NAME, COLUMN_NAME
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
    (TABLE_NAME = 'product' AND COLUMN_NAME IN ('id', 'quantity'))
    OR (TABLE_NAME LIKE '%size%' AND COLUMN_NAME LIKE '%quantity%')
  )
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 1) Sumar size_quantity por producto y dejar el resultado en product.quantity
UPDATE product p
SET p.quantity = COALESCE((
  SELECT SUM(ps.size_quantity)
  FROM product_sizes ps
  WHERE ps.product_id = p.id
), p.quantity);

-- 2) Eliminar la columna size_quantity de la tabla de la colección
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND TABLE_NAME = 'product_sizes'
    AND COLUMN_NAME = 'size_quantity'
);
SET @sql := IF(@col_exists > 0,
  'ALTER TABLE product_sizes DROP COLUMN size_quantity',
  'SELECT ''size_quantity ya no existe, nada que borrar'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

COMMIT;

-- Verificación
SELECT
  (SELECT COUNT(*) FROM product) AS total_productos,
  (SELECT COUNT(*) FROM product_sizes) AS total_variantes,
  (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND TABLE_NAME = 'product_sizes'
      AND COLUMN_NAME = 'size_quantity') AS size_quantity_aun_existe;