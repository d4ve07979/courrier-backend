package tg.inseed.gestioncourrier.gestioncourrier.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration Spring pour activer l'audit JPA.
 * Permet l'utilisation des annotations @CreatedDate et @LastModifiedDate
 * 
 * @author KENKOU Marê Dave Christian
 * @version 1.0
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // L'annotation @EnableJpaAuditing active automatiquement l'audit
}