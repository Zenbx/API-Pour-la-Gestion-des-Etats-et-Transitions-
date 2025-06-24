-- ================================
-- SCHEMA DE BASE DE DONNÉES POUR L'API PETRI STATE TRANSITION (R2DBC-compatible)
-- ================================

DROP TABLE IF EXISTS service_composite_resources;
DROP TABLE IF EXISTS service_unit_resources;
DROP TABLE IF EXISTS composite_unit_resources;
DROP TABLE IF EXISTS transitions;
DROP TABLE IF EXISTS composite_resources;
DROP TABLE IF EXISTS unit_resources;
DROP TABLE IF EXISTS services;

-- ================================
-- TABLE SERVICES
-- ================================
CREATE TABLE services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    state VARCHAR(20) NOT NULL DEFAULT 'PLANIFIE',
    type VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMALE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    max_execution_time_minutes INT DEFAULT NULL,
    auto_retry BOOLEAN DEFAULT FALSE,

    INDEX idx_services_state (state),
    INDEX idx_services_type (type),
    INDEX idx_services_priority (priority),
    INDEX idx_services_created_at (created_at)
);

-- ================================
-- TABLE UNIT_RESOURCES
-- ================================
CREATE TABLE unit_resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    state VARCHAR(20) NOT NULL DEFAULT 'LIBRE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NULL,
    location VARCHAR(200),
    capacity INT DEFAULT NULL,
    current_load INT DEFAULT 0,

    INDEX idx_unit_resources_state (state),
    INDEX idx_unit_resources_location (location),
    INDEX idx_unit_resources_created_at (created_at)
);

-- ================================
-- TABLE COMPOSITE_RESOURCES
-- ================================
CREATE TABLE composite_resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    state VARCHAR(20) NOT NULL DEFAULT 'VIDE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NULL,
    location VARCHAR(200),
    total_capacity INT DEFAULT NULL,
    min_required_components INT DEFAULT 1,

    INDEX idx_composite_resources_state (state),
    INDEX idx_composite_resources_location (location),
    INDEX idx_composite_resources_created_at (created_at)
);

-- ================================
-- TABLE TRANSITIONS
-- ================================
CREATE TABLE transitions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    name VARCHAR(200),
    description VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    error_message TEXT,
    metadata_json JSON,

    INDEX idx_transitions_type (type),
    INDEX idx_transitions_status (status),
    INDEX idx_transitions_created_at (created_at)
);

-- ================================
-- TABLES DE RELATIONS
-- ================================

CREATE TABLE service_unit_resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_id BIGINT NOT NULL,
    unit_resource_id BIGINT NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_su_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_su_unit FOREIGN KEY (unit_resource_id) REFERENCES unit_resources(id) ON DELETE CASCADE,
    CONSTRAINT unique_service_unit_resource UNIQUE (service_id, unit_resource_id),
    INDEX idx_service_unit_resources_service (service_id),
    INDEX idx_service_unit_resources_unit_resource (unit_resource_id)
);

CREATE TABLE service_composite_resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_id BIGINT NOT NULL,
    composite_resource_id BIGINT NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sc_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_sc_composite FOREIGN KEY (composite_resource_id) REFERENCES composite_resources(id) ON DELETE CASCADE,
    CONSTRAINT unique_service_composite_resource UNIQUE (service_id, composite_resource_id),
    INDEX idx_service_composite_resources_service (service_id),
    INDEX idx_service_composite_resources_composite_resource (composite_resource_id)
);

CREATE TABLE composite_unit_resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    composite_resource_id BIGINT NOT NULL,
    unit_resource_id BIGINT NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cu_composite FOREIGN KEY (composite_resource_id) REFERENCES composite_resources(id) ON DELETE CASCADE,
    CONSTRAINT fk_cu_unit FOREIGN KEY (unit_resource_id) REFERENCES unit_resources(id) ON DELETE CASCADE,
    CONSTRAINT unique_composite_unit_resource UNIQUE (composite_resource_id, unit_resource_id),
    INDEX idx_composite_unit_resources_composite (composite_resource_id),
    INDEX idx_composite_unit_resources_unit (unit_resource_id)
);
