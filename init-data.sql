-- Script d'initialisation des données de référence
-- À exécuter dans PostgreSQL

-- ============================================
-- 1. TYPES DE COURRIER
-- ============================================

-- Vider la table si elle existe
TRUNCATE TABLE type_courrier CASCADE;

-- Insérer les types de courrier
INSERT INTO type_courrier (id_type_courrier, libelle, description) VALUES
(1, 'ENTRANT', 'Courrier reçu de l''extérieur'),
(2, 'SORTANT', 'Courrier envoyé vers l''extérieur'),
(3, 'NOTE_INTERNE', 'Note de service interne');

-- Réinitialiser la séquence
SELECT setval('type_courrier_id_type_courrier_seq', (SELECT MAX(id_type_courrier) FROM type_courrier));

-- ============================================
-- 2. STATUTS
-- ============================================

-- Vider la table si elle existe
TRUNCATE TABLE statut CASCADE;

-- Insérer les statuts
INSERT INTO statut (id_statut, libelle_statut) VALUES
(1, 'EN_ATTENTE'),
(2, 'AFFECTE'),
(3, 'EN_TRAITEMENT'),
(4, 'TRAITE'),
(5, 'VALIDE'),
(6, 'ENVOYE'),
(7, 'CLASSE'),
(8, 'ARCHIVE');

-- Réinitialiser la séquence
SELECT setval('statut_id_statut_seq', (SELECT MAX(id_statut) FROM statut));

-- ============================================
-- 3. EXPÉDITEURS (Exemples)
-- ============================================

-- Vider la table si elle existe (optionnel)
-- TRUNCATE TABLE expediteur CASCADE;

-- Insérer des expéditeurs d'exemple
INSERT INTO expediteur (nom_expediteur, adresse_expediteur, telephone_expediteur, email_expediteur) VALUES
('Ministere de l''Education', 'Lome, Togo', '+228 22 21 45 67', 'contact@education.tg'),
('Ministere de la Sante', 'Lome, Togo', '+228 22 21 78 90', 'contact@sante.tg'),
('Ministere de l''Economie', 'Lome, Togo', '+228 22 00 00 00', 'contact@economie.gouv.tg')
ON CONFLICT DO NOTHING;

-- ============================================
-- 4. DESTINATAIRES (Exemples)
-- ============================================

-- Vider la table si elle existe (optionnel)
-- TRUNCATE TABLE destinataire CASCADE;

-- Insérer des destinataires d'exemple
INSERT INTO destinataire (nom_destinataire, adresse_destinataire, telephone_destinataire, email_destinataire) VALUES
('INSEED', 'Lomé, Togo', '+228 22 25 89 45', 'contact@inseed.tg'),
('Direction Générale', 'INSEED, Lomé', '+228 22 25 89 46', 'dg@inseed.tg')
ON CONFLICT DO NOTHING;

-- ============================================
-- 5. FICHES DE TRANSMISSION (Exemple)
-- ============================================

-- Vider la table si elle existe (optionnel)
-- TRUNCATE TABLE fiche_de_transmission CASCADE;

-- Insérer une fiche par défaut
INSERT INTO fiche_de_transmission (id_fiche, nom_fiche, description_fiche) VALUES
(1, 'Fiche Standard', 'Fiche de transmission standard')
ON CONFLICT DO NOTHING;

-- Réinitialiser la séquence si nécessaire
-- SELECT setval('fiche_de_transmission_id_fiche_seq', (SELECT MAX(id_fiche) FROM fiche_de_transmission));

-- ============================================
-- 6. TABLE SPRING_SESSION (Correction erreur)
-- ============================================

-- Créer la table spring_session si elle n'existe pas
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

-- Vérifier les données insérées
SELECT 'Types de courrier:' as table_name, COUNT(*) as count FROM type_courrier UNION ALL

SELECT 'Statuts:', COUNT(*) FROM statut
UNION ALL
SELECT 'Expéditeurs:', COUNT(*) FROM expediteur
UNION ALL
SELECT 'Destinataires:', COUNT(*) FROM destinataire
UNION ALL
SELECT 'Fiches:', COUNT(*) FROM fiche_de_transmission;

-- Afficher les types de courrier
SELECT * FROM type_courrier ORDER BY id_type_courrier;

-- Afficher les statuts
SELECT * FROM statut ORDER BY id_statut;

COMMIT;
