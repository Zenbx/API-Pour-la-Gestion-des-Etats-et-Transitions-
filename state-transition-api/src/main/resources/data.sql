-- ================================
-- DONNÉES DE TEST POUR L'API PETRI STATE TRANSITION
-- ================================

-- Suppression des données existantes (pour les tests)
DELETE FROM service_composite_resources;
DELETE FROM service_unit_resources;
DELETE FROM composite_unit_resources;
DELETE FROM transitions;
DELETE FROM composite_resources;
DELETE FROM unit_resources;
DELETE FROM services;

-- ================================
-- DONNÉES DE TEST - RESSOURCES UNITAIRES
-- ================================
INSERT INTO unit_resources (name, description, state, location, capacity, current_load, created_at) VALUES
('CPU-Core-01', 'Processeur haute performance - Core 1', 'LIBRE', 'Datacenter-A-Rack-01', 100, 0, NOW()),
('CPU-Core-02', 'Processeur haute performance - Core 2', 'LIBRE', 'Datacenter-A-Rack-01', 100, 0, NOW()),
('CPU-Core-03', 'Processeur haute performance - Core 3', 'AFFECTE', 'Datacenter-A-Rack-02', 100, 75, NOW()),
('CPU-Core-04', 'Processeur haute performance - Core 4', 'OCCUPE', 'Datacenter-A-Rack-02', 100, 95, NOW()),

('Memory-Bank-01', 'Banque mémoire 16GB DDR4', 'LIBRE', 'Datacenter-A-Rack-01', 16384, 0, NOW()),
('Memory-Bank-02', 'Banque mémoire 16GB DDR4', 'LIBRE', 'Datacenter-A-Rack-01', 16384, 0, NOW()),
('Memory-Bank-03', 'Banque mémoire 16GB DDR4', 'AFFECTE', 'Datacenter-A-Rack-02', 16384, 8192, NOW()),
('Memory-Bank-04', 'Banque mémoire 16GB DDR4', 'OCCUPE', 'Datacenter-A-Rack-02', 16384, 14000, NOW()),

('Storage-SSD-01', 'Disque SSD 1TB NVMe', 'LIBRE', 'Datacenter-A-Rack-03', 1024, 0, NOW()),
('Storage-SSD-02', 'Disque SSD 1TB NVMe', 'LIBRE', 'Datacenter-A-Rack-03', 1024, 0, NOW()),
('Storage-SSD-03', 'Disque SSD 1TB NVMe', 'AFFECTE', 'Datacenter-A-Rack-04', 1024, 512, NOW()),
('Storage-SSD-04', 'Disque SSD 1TB NVMe', 'OCCUPE', 'Datacenter-A-Rack-04', 1024, 890, NOW()),

('Network-Port-01', 'Port réseau 10Gbps', 'LIBRE', 'Datacenter-A-Switch-01', 10, 0, NOW()),
('Network-Port-02', 'Port réseau 10Gbps', 'LIBRE', 'Datacenter-A-Switch-01', 10, 0, NOW()),
('Network-Port-03', 'Port réseau 10Gbps', 'AFFECTE', 'Datacenter-A-Switch-02', 10, 7, NOW()),
('Network-Port-04', 'Port réseau 10Gbps', 'OCCUPE', 'Datacenter-A-Switch-02', 10, 9, NOW()),

('GPU-Tesla-01', 'GPU Tesla V100 pour calcul intensif', 'LIBRE', 'Datacenter-B-Rack-01', 1, 0, NOW()),
('GPU-Tesla-02', 'GPU Tesla V100 pour calcul intensif', 'INDISPONIBLE', 'Datacenter-B-Rack-01', 1, 0, NOW()),

('Database-Connection-01', 'Connexion base de données MySQL', 'LIBRE', 'Database-Pool-Primary', 1, 0, NOW()),
('Database-Connection-02', 'Connexion base de données MySQL', 'LIBRE', 'Database-Pool-Primary', 1, 0, NOW()),
('Database-Connection-03', 'Connexion base de données MySQL', 'OCCUPE', 'Database-Pool-Primary', 1, 1, NOW());

-- ================================
-- DONNÉES DE TEST - RESSOURCES COMPOSITES
-- ================================
INSERT INTO composite_resources (name, description, state, location, total_capacity, min_required_components, created_at) VALUES
('Server-Node-01', 'Serveur complet avec CPU, RAM et stockage', 'VIDE', 'Datacenter-A-Rack-01', 1000, 3, NOW()),
('Server-Node-02', 'Serveur complet avec CPU, RAM et stockage', 'PRET', 'Datacenter-A-Rack-02', 1000, 3, NOW()),
('Server-Node-03', 'Serveur complet avec CPU, RAM et stockage', 'AFFECTE', 'Datacenter-A-Rack-03', 1000, 3, NOW()),

('HPC-Cluster-01', 'Cluster de calcul haute performance', 'VIDE', 'Datacenter-B', 5000, 2, NOW()),
('HPC-Cluster-02', 'Cluster de calcul haute performance', 'INDISPONIBLE', 'Datacenter-B', 5000, 2, NOW()),

('Database-Pool-Primary', 'Pool de connexions base de données primaire', 'PRET', 'Database-Farm', 100, 2, NOW()),
('Load-Balancer-01', 'Équilibreur de charge avec ports réseau', 'VIDE', 'Network-DMZ', 50, 2, NOW());

-- ================================
-- RELATIONS COMPOSITE -> UNIT RESOURCES
-- ================================
-- Server-Node-01 components
INSERT INTO composite_unit_resources (composite_resource_id, unit_resource_id, is_required, created_at) VALUES
(1, 1, TRUE, NOW()), -- CPU-Core-01
(1, 5, TRUE, NOW()), -- Memory-Bank-01
(1, 9, TRUE, NOW()); -- Storage-SSD-01

-- Server-Node-02 components
INSERT INTO composite_unit_resources (composite_resource_id, unit_resource_id, is_required, created_at) VALUES
(2, 3, TRUE, NOW()), -- CPU-Core-03 (AFFECTE)
(2, 7, TRUE, NOW()), -- Memory-Bank-03 (AFFECTE)
(2, 11, TRUE, NOW()); -- Storage-SSD-03 (AFFECTE)

-- Server-Node-03 components
INSERT INTO composite_unit_resources (composite_resource_id, unit_resource_id, is_required, created_at) VALUES
(3, 4, TRUE, NOW()), -- CPU-Core-04 (OCCUPE)
(3, 8, TRUE, NOW()), -- Memory-Bank-04 (OCCUPE)
(3, 12, TRUE, NOW()); -- Storage-SSD-04 (OCCUPE)

-- HPC-Cluster-01 components
INSERT INTO composite_unit_resources (composite_resource_id, unit_resource_id, is_required, created_at) VALUES
(4, 17, TRUE, NOW()), -- GPU-Tesla-01
(4, 2, TRUE, NOW());  -- CPU-Core-02

-- Database-Pool-Primary components
INSERT INTO composite_unit_resources (composite_resource_id, unit_resource_id, is_required, created_at) VALUES
(6, 19, TRUE, NOW()), -- Database-Connection-01
(6, 20, TRUE, NOW()); -- Database-Connection-02

-- Load-Balancer-01 components
INSERT INTO composite_unit_resources (composite_resource_id, unit_resource_id, is_required, created_at) VALUES
(7, 13, TRUE, NOW()), -- Network-Port-01
(7, 14, TRUE, NOW()); -- Network-Port-02

-- ================================
-- DONNÉES DE TEST - SERVICES
-- ================================
INSERT INTO services (name, description, state, type, priority, max_execution_time_minutes, auto_retry, created_at) VALUES
('Web-Service-Frontend', 'Service web frontend pour interface utilisateur', 'PLANIFIE', 'NON_BLOQUANT', 'NORMALE', 30, FALSE, NOW()),
('Data-Processing-ETL', 'Service de traitement ETL pour données massives', 'PUBLIE', 'BLOQUANT', 'HAUTE', 120, TRUE, NOW()),
('Machine-Learning-Training', 'Entraînement de modèles de machine learning', 'PRET', 'BLOQUANT', 'CRITIQUE', 240, FALSE, NOW()),
('Database-Backup-Service', 'Service de sauvegarde automatique des bases de données', 'EN_COURS', 'NON_BLOQUANT', 'NORMALE', 60, TRUE, NOW()),
('API-Gateway-Service', 'Gateway API pour routage des requêtes', 'PRET', 'BLOQUANT', 'CRITIQUE', 0, FALSE, NOW()),

('Real-Time-Analytics', 'Analyse en temps réel des données streaming', 'BLOQUE', 'NON_BLOQUANT', 'HAUTE', 0, TRUE, NOW()),
('File-Processing-Service', 'Service de traitement de fichiers en lot', 'RETARDE', 'NON_BLOQUANT', 'BASSE', 90, FALSE, NOW()),
('Notification-Service', 'Service de notifications push et email', 'EN_PAUSE', 'NON_BLOQUANT', 'NORMALE', 15, TRUE, NOW()),
('Security-Scanning-Service', 'Service de scan de sécurité automatique', 'TERMINE', 'BLOQUANT', 'CRITIQUE', 45, FALSE, NOW() - INTERVAL 2 HOUR),
('Log-Aggregation-Service', 'Agrégation et indexation des logs système', 'ANNULE', 'NON_BLOQUANT', 'BASSE', 30, FALSE, NOW() - INTERVAL 1 HOUR);

-- ================================
-- RELATIONS SERVICE -> UNIT RESOURCES
-- ================================
-- Web-Service-Frontend -> ressources légères
INSERT INTO service_unit_resources (service_id, unit_resource_id, is_required, created_at) VALUES
(1, 1, TRUE, NOW()), -- CPU-Core-01
(1, 5, TRUE, NOW()); -- Memory-Bank-01

-- Data-Processing-ETL -> ressources intensives
INSERT INTO service_unit_resources (service_id, unit_resource_id, is_required, created_at) VALUES
(2, 2, TRUE, NOW()), -- CPU-Core-02
(2, 6, TRUE, NOW()), -- Memory-Bank-02
(2, 10, TRUE, NOW()); -- Storage-SSD-02

-- Machine-Learning-Training -> GPU + CPU
INSERT INTO service_unit_resources (service_id, unit_resource_id, is_required, created_at) VALUES
(3, 17, TRUE, NOW()), -- GPU-Tesla-01
(3, 2, TRUE, NOW());  -- CPU-Core-02

-- Database-Backup-Service -> stockage + connexions DB
INSERT INTO service_unit_resources (service_id, unit_resource_id, is_required, created_at) VALUES
(4, 9, TRUE, NOW()),  -- Storage-SSD-01
(4, 19, TRUE, NOW()); -- Database-Connection-01

-- API-Gateway-Service -> réseau
INSERT INTO service_unit_resources (service_id, unit_resource_id, is_required, created_at) VALUES
(5, 13, TRUE, NOW()), -- Network-Port-01
(5, 14, TRUE, NOW()); -- Network-Port-02

-- ================================
-- RELATIONS SERVICE -> COMPOSITE RESOURCES
-- ================================
-- Web-Service-Frontend -> Server complet
INSERT INTO service_composite_resources (service_id, composite_resource_id, is_required, created_at) VALUES
(1, 1, FALSE, NOW()); -- Server-Node-01 (optionnel pour NON_BLOQUANT)

-- Data-Processing-ETL -> Server dédié
INSERT INTO service_composite_resources (service_id, composite_resource_id, is_required, created_at) VALUES
(2, 2, TRUE, NOW()); -- Server-Node-02

-- Machine-Learning-Training -> HPC Cluster
INSERT INTO service_composite_resources (service_id, composite_resource_id, is_required, created_at) VALUES
(3, 4, TRUE, NOW()); -- HPC-Cluster-01

-- Database-Backup-Service -> Database Pool
INSERT INTO service_composite_resources (service_id, composite_resource_id, is_required, created_at) VALUES
(4, 6, TRUE, NOW()); -- Database-Pool-Primary

-- API-Gateway-Service -> Load Balancer
INSERT INTO service_composite_resources (service_id, composite_resource_id, is_required, created_at) VALUES
(5, 7, TRUE, NOW()); -- Load-Balancer-01

-- ================================
-- DONNÉES DE TEST - TRANSITIONS
-- ================================
INSERT INTO transitions (type, status, name, description, created_at, started_at, completed_at, metadata_json) VALUES
('NORMALE', 'TERMINEE', 'Service_1_Planification', 'Planification du service Web-Service-Frontend', NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 55 MINUTE, '{"serviceId": 1, "fromState": "PLANIFIE", "toState": "PUBLIE"}'),
('NORMALE', 'TERMINEE', 'Service_2_Publication', 'Publication du service Data-Processing-ETL', NOW() - INTERVAL 45 MINUTE, NOW() - INTERVAL 45 MINUTE, NOW() - INTERVAL 40 MINUTE, '{"serviceId": 2, "fromState": "PLANIFIE", "toState": "PUBLIE"}'),
('SYNCHRONE', 'TERMINEE', 'Service_4_Demarrage', 'Démarrage synchrone du service Database-Backup avec allocation ressources', NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 25 MINUTE, '{"serviceId": 4, "resourcesAllocated": [9, 19], "compositeResources": [6]}'),

('AUTOMATIQUE', 'EN_COURS', 'Auto_Resource_Check', 'Vérification automatique de la disponibilité des ressources', NOW() - INTERVAL 10 MINUTE, NOW() - INTERVAL 10 MINUTE, NULL, '{"checkType": "availability", "threshold": 80}'),
('NORMALE', 'EN_ATTENTE', 'Service_3_Preparation', 'Préparation du service Machine-Learning-Training', NOW() - INTERVAL 5 MINUTE, NULL, NULL, '{"serviceId": 3, "estimatedDuration": 240}'),

('SYNCHRONE', 'ECHOUEE', 'Service_6_Allocation_Failed', 'Échec allocation ressources pour Real-Time-Analytics', NOW() - INTERVAL 20 MINUTE, NOW() - INTERVAL 20 MINUTE, NOW() - INTERVAL 18 MINUTE, '{"serviceId": 6, "error": "ResourceAllocationException", "reason": "Ressources insuffisantes"}'),
('AUTOMATIQUE', 'TERMINEE', 'Cleanup_Old_Transitions', 'Nettoyage automatique des anciennes transitions', NOW() - INTERVAL 60 MINUTE, NOW() - INTERVAL 60 MINUTE, NOW() - INTERVAL 58 MINUTE, '{"deletedCount": 25, "olderThan": "7 days"}'),

('NORMALE', 'TERMINEE', 'Service_9_Completion', 'Finalisation du service Security-Scanning-Service', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR + INTERVAL 5 MINUTE, '{"serviceId": 9, "duration": 45, "scanResults": "Clean"}'),
('NORMALE', 'TERMINEE', 'Service_10_Cancellation', 'Annulation du service Log-Aggregation-Service', NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR + INTERVAL 2 MINUTE, '{"serviceId": 10, "reason": "Resource conflict", "cancelled_by": "admin"}');

-- ================================
-- MISE À JOUR DES TIMESTAMPS POUR COHÉRENCE
-- ================================
UPDATE services SET
    started_at = NOW() - INTERVAL 25 MINUTE,
    updated_at = NOW() - INTERVAL 20 MINUTE
WHERE id = 4; -- Database-Backup-Service en cours

UPDATE services SET
    completed_at = NOW() - INTERVAL 2 HOUR + INTERVAL 5 MINUTE,
    updated_at = NOW() - INTERVAL 2 HOUR + INTERVAL 5 MINUTE
WHERE id = 9; -- Security-Scanning-Service terminé

UPDATE services SET
    updated_at = NOW() - INTERVAL 1 HOUR + INTERVAL 2 MINUTE
WHERE id = 10; -- Log-Aggregation-Service annulé

-- ================================
-- VÉRIFICATION DES DONNÉES INSÉRÉES
-- ================================
-- Les requêtes suivantes peuvent être utilisées pour vérifier l'insertion :

-- SELECT 'Services' as entity_type, COUNT(*) as count FROM services
-- UNION ALL
-- SELECT 'Unit Resources', COUNT(*) FROM unit_resources
-- UNION ALL
-- SELECT 'Composite Resources', COUNT(*) FROM composite_resources
-- UNION ALL
-- SELECT 'Transitions', COUNT(*) FROM transitions
-- UNION ALL
-- SELECT 'Service-Unit Relations', COUNT(*) FROM service_unit_resources
-- UNION ALL
-- SELECT 'Service-Composite Relations', COUNT(*) FROM service_composite_resources
-- UNION ALL
-- SELECT 'Composite-Unit Relations', COUNT(*) FROM composite_unit_resources;