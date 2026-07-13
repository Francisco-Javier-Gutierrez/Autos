package com.utsem.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;

@Configuration
public class DatabaseConfig {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeTriggers() {
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_detprod_after_update");
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_detprod_after_insert");
        
        jdbcTemplate.execute("""
            CREATE TRIGGER trg_detprod_after_update
            AFTER UPDATE ON det_prod
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
            END
        """);

        jdbcTemplate.execute("""
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
            END
        """);
    }
}
