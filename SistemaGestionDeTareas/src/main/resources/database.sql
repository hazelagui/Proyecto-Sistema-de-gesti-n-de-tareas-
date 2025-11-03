-- ===========================================
-- Script limpio y actualizado para MySQL 8.4+ (v3)
-- - Sin DROP INDEX (no es necesario tras DROP DATABASE)
-- ===========================================

-- Elimina la base de datos anterior si existe
DROP DATABASE IF EXISTS gestion_tareas;
CREATE DATABASE gestion_tareas;
USE gestion_tareas;

-- ===========================================
-- Tablas principales
-- ===========================================

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    es_admin BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE proyectos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NULL,
    id_responsable INT NOT NULL,
    nivel_riesgo VARCHAR(20) NOT NULL DEFAULT 'VERDE',
    presupuesto_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_responsable) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE tareas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento TIMESTAMP NOT NULL,
    id_proyecto INT NOT NULL,
    id_responsable INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    comentarios TEXT,
    FOREIGN KEY (id_proyecto) REFERENCES proyectos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_responsable) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE reportes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    id_referencia INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    contenido TEXT,
    fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_generador INT NOT NULL,
    FOREIGN KEY (id_generador) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE costos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL, -- 'PROYECTO' o 'TAREA'
    id_referencia INT NOT NULL, -- ID del proyecto o tarea
    descripcion VARCHAR(255) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    tipo_costo VARCHAR(20) NOT NULL, -- 'RETRASO', 'ADELANTO', 'GASTO_PLANIFICADO'
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_usuario_registro INT NOT NULL,
    FOREIGN KEY (id_usuario_registro) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ===========================================
-- Índices
-- ===========================================

CREATE INDEX idx_costos_referencia ON costos(tipo, id_referencia);
CREATE INDEX idx_costos_tipo ON costos(tipo_costo);
CREATE INDEX idx_costos_fecha ON costos(fecha_registro);

-- ===========================================
-- Datos iniciales (sintaxis moderna para 8.4+)
-- ===========================================

INSERT INTO usuarios (nombre, apellido, email, password, es_admin)
VALUES ('Admin', 'Sistema', 'admin@sistema.com', 'admin123', TRUE)
AS new
ON DUPLICATE KEY UPDATE
  nombre = new.nombre,
  apellido = new.apellido,
  password = new.password,
  es_admin = new.es_admin;

INSERT INTO usuarios (nombre, apellido, email, password, es_admin)
VALUES ('Usuario', 'Regular', 'usuario@sistema.com', 'user123', FALSE)
AS new
ON DUPLICATE KEY UPDATE
  nombre = new.nombre,
  apellido = new.apellido,
  password = new.password,
  es_admin = new.es_admin;
