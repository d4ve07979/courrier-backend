-- Script pour corriger le problème de la fiche obligatoire

-- 1. Créer la fiche par défaut si elle n'existe pas
INSERT INTO fiche_de_transmission (id_fiche, nom_fiche, description_fiche) 
VALUES (1, 'Fiche Standard', 'Fiche de transmission standard')
ON CONFLICT (id_fiche) DO NOTHING;

-- 2. Rendre la colonne id_fiche nullable dans la table courrier
ALTER TABLE courrier ALTER COLUMN id_fiche DROP NOT NULL;

-- 3. Vérifier
SELECT * FROM fiche_de_transmission;

COMMIT;
