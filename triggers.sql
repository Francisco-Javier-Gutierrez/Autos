-- Triggers para actualizar el estado del producto a "Agotado" cuando el stock llega a 0
USE autos;

DROP TRIGGER IF EXISTS trg_detprod_after_update;
DROP TRIGGER IF EXISTS trg_detprod_after_insert;

DELIMITER //

CREATE TRIGGER trg_detprod_after_update
AFTER UPDATE ON det_prod
FOR EACH ROW
BEGIN
    DECLARE total_stock INT;
    
    -- Calcular el stock total de todos los detalles (colores/transmisiones) de este producto
    SELECT SUM(stock) INTO total_stock 
    FROM det_prod 
    WHERE product_id = NEW.product_id;
    
    -- Si el stock total es 0 o menor, actualizar el estado del producto a Agotado
    IF total_stock <= 0 THEN
        UPDATE productos 
        SET estado = 'Agotado' 
        WHERE id = NEW.product_id;
    ELSEIF total_stock > 0 THEN
        UPDATE productos 
        SET estado = 'Disponible' 
        WHERE id = NEW.product_id AND estado = 'Agotado';
    END IF;
END //

CREATE TRIGGER trg_detprod_after_insert
AFTER INSERT ON det_prod
FOR EACH ROW
BEGIN
    DECLARE total_stock INT;
    
    SELECT SUM(stock) INTO total_stock 
    FROM det_prod 
    WHERE product_id = NEW.product_id;
    
    IF total_stock <= 0 THEN
        UPDATE productos 
        SET estado = 'Agotado' 
        WHERE id = NEW.product_id;
    ELSEIF total_stock > 0 THEN
        UPDATE productos 
        SET estado = 'Disponible' 
        WHERE id = NEW.product_id AND estado = 'Agotado';
    END IF;
END //

DELIMITER ;
