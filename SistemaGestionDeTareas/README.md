# Sistema de Gestión de Tareas y Proyectos

Un sistema completo para administrar proyectos y tareas, permitiendo asignar responsables, seguir el progreso y generar informes de avance.

## Descripción

Este sistema permite gestionar proyectos y tareas en una empresa de desarrollo de software, facilitando:

- Registro y seguimiento de proyectos
- Asignación de tareas a miembros del equipo
- Seguimiento del progreso de las tareas
- Clasificación de proyectos según su nivel de riesgo
- Generación de reportes de avance

## Tecnologías utilizadas

- Java 17
- Swing para la interfaz gráfica
- MySQL para la base de datos
- Patrón MVC (Modelo-Vista-Controlador)
- Arquitectura Cliente-Servidor (en desarrollo)

## Características

- Autenticación de usuarios con diferentes roles (Administrador/Usuario)
- Gestión completa de proyectos
- Seguimiento de tareas con estados (Pendiente, En Proceso, Completada)
- Tablero visual de tareas
- Clasificación de riesgos por color (Verde, Amarillo, Rojo)
- Persistencia de datos en MySQL

## Estructura del proyecto

La aplicación utiliza el patrón de diseño MVC (Modelo-Vista-Controlador):

- **Modelo**: Contiene las clases de entidad y la lógica de acceso a datos.
- **Vista**: Interfaces de usuario implementadas con Swing.
- **Controlador**: Lógica de negocio que conecta las vistas con los modelos.
- **DAO**: Clases para acceso a la base de datos siguiendo el patrón DAO (Data Access Object).
- **Util**: Clases utilitarias para la conexión a la base de datos y gestión de errores.

## Instrucciones de configuración

### Requisitos

- Java JDK 17 o superior
- NetBeans IDE 17 o superior
- MySQL 8.0 o superior
- Maven (para gestión de dependencias)

### Configuración de la base de datos

1. Instale MySQL Server 8.0 o superior
2. Cree una base de datos llamada `gestion_tareas`
3. Ejecute el script `database.sql` para crear las tablas y datos iniciales
4. Configure las credenciales de acceso en el archivo `src/main/java/com/mycompany/sistemagestiondetareas/util/ConexionBD.java`

```java
private static final String URL = "jdbc:mysql://localhost:3306/SistemaGestionDeTareas";
private static final String USUARIO = "root";  
private static final String PASSWORD = "password"; 
```

### Compilación e instalación

1. Clone o descargue el repositorio
2. Abra el proyecto en NetBeans IDE
3. Compile el proyecto con Maven: `mvn clean package`
4. Ejecute la aplicación desde NetBeans o mediante el comando:
   `java -jar target/SistemaGestionDeTareas-1.0-SNAPSHOT.jar`

## Uso del sistema

### Acceso al sistema

- **Administrador**:
  - Email: admin@sistema.com
  - Contraseña: admin123

- **Usuario**:
  - Email: usuario@sistema.com
  - Contraseña: user123

### Funcionalidades principales

- **Administradores**:
  - Gestión completa de usuarios, proyectos y tareas
  - Asignación de tareas a usuarios
  - Generación de reportes

- **Usuarios regulares**:
  - Visualización de proyectos asignados
  - Actualización del estado de sus tareas
  - Añadir comentarios a las tareas

## Estado del proyecto

El proyecto actualmente incluye:

- Autenticación de usuarios
- Gestión de proyectos y tareas
- Asignación de responsabilidades
- Seguimiento de estado
- Persistencia de datos en MySQL

## Desarrollos futuros

- Mejora de la interfaz de usuario
- Implementación de notificaciones automáticas
- Integración con servicios de correo electrónico
- Exportación de reportes a PDF

## Autores

Desarrollado para el curso de Programación Orientada a Objetos.

## Licencia

Este proyecto es de uso educativo. 