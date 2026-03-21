-- Script d'initialisation simplifié (sans caractères spéciaux)
-- À exécuter dans PostgreSQL

-- ============================================
-- 1. TYPES DE COURRIER
-- ============================================

TRUNCATE TABLE type_courrier CASCADE;

INSERT INTO type_courrier (id_type_courrier, libelle, description) VALUES
(1, 'ENTRANT', 'Courrier recu de exterieur'),
(2, 'SORTANT', 'Courrier envoye vers exterieur'),
(3, 'NOTE_INTERNE', 'Note de service interne');

SELECT setval('type_courrier_id_type_courrier_seq', (SELECT MAX(id_type_courrier) FROM type_courrier));

-- ============================================
-- 2. STATUTS
-- ============================================

TRUNCATE TABLE statut CASCADE;

INSERT INTO statut (id_statut, libelle_statut) VALUES
(1, 'EN_ATTENTE'),
(2, 'AFFECTE'),
(3, 'EN_TRAITEMENT'),
(4, 'TRAITE'),
(5, 'VALIDE'),
(6, 'ENVOYE'),
(7, 'CLASSE'),
(8, 'ARCHIVE');

SELECT setval('statut_id_statut_seq', (SELECT MAX(id_statut) FROM statut));

-- ============================================
-- 3. FICHES DE TRANSMISSION
-- ============================================

INSERT INTO fiche_de_transmission (id_fiche, nom_fiche, description_fiche) VALUES
(1, 'Fiche Standard', 'Fiche de transmission standard')
ON CONFLICT DO NOTHING;

-- ============================================
-- 4. TABLE SPRING_SESSION
-- ============================================

CREATE TABLE IF NOT EXISTS spring_session (
    primary_id CHAR(36) NOT NULL,
    session_id CHAR(36) NOT NULL,
    creation_time BIGINT NOT NULL,
    last_access_time BIGINT NOT NULL,
    max_inactive_interval INT NOT NULL,
    expiry_time BIGINT NOT NULL,
    principal_name VARCHAR(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS spring_session_ix1 ON spring_session (session_id);
CREATE INDEX IF NOT EXISTS spring_session_ix2 ON spring_session (expiry_time);
CREATE INDEX IF NOT EXISTS spring_session_ix3 ON spring_session (principal_name);

CREATE TABLE IF NOT EXISTS spring_session_attributes (
    session_primary_id CHAR(36) NOT NULL,
    attribute_name VARCHAR(200) NOT NULL,
    attribute_bytes BYTEA NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id) REFERENCES spring_session(primary_id) ON DELETE CASCADE
);

-- ============================================
-- VÉRIFICATION
-- ============================================

SELECT 'Types de courrier:' as table_name, COUNT(*) as count FROM type_courrier
UNION ALL
SELECT 'Statuts:', COUNT(*) FROM statut;

SELECT * FROM type_courrier ORDER BY id_type_courrier;
SELECT * FROM statut ORDER BY id_statut;

COMMIT;
