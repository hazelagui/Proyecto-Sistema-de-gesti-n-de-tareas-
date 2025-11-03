-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS gestion_tareas;
USE gestion_tareas;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    es_admin BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de proyectos
CREATE TABLE IF NOT EXISTS proyectos (
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

-- Tabla de tareas
CREATE TABLE IF NOT EXISTS tareas (
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

-- Tabla de reportes
CREATE TABLE IF NOT EXISTS reportes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    id_referencia INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    contenido TEXT,
    fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_generador INT NOT NULL,
    FOREIGN KEY (id_generador) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Tabla de costos
CREATE TABLE IF NOT EXISTS costos (
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

-- Índices para la tabla de costos
CREATE INDEX idx_costos_referencia ON costos(tipo, id_referencia);
CREATE INDEX idx_costos_tipo ON costos(tipo_costo);
CREATE INDEX idx_costos_fecha ON costos(fecha_registro);

-- Insertar datos de ejemplo básicos (usuario admin y usuario regular)
INSERT INTO usuarios (nombre, apellido, email, password, es_admin)
VALUES ('Admin', 'Sistema', 'admin@sistema.com', 'admin123', TRUE)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), apellido=VALUES(apellido), password=VALUES(password), es_admin=VALUES(es_admin);  -- password: admin123

INSERT INTO usuarios (nombre, apellido, email, password, es_admin)
VALUES ('Usuario', 'Regular', 'usuario@sistema.com', 'user123', FALSE)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), apellido=VALUES(apellido), password=VALUES(password), es_admin=VALUES(es_admin);  -- password: user123

-- Nota: Para cargar datos de ejemplo más completos, ejecutar el script datos_ejemplo.sql
-- después de este script. 