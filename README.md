# SGA — Sistema de Gestión de Agenda

Plataforma integral para la administración de talleres educativos, colegios, usuarios y agendas escolares. Desarrollada con una arquitectura desacoplada: backend REST en Spring Boot y cliente de escritorio en JavaFX.

> 🌐 **[Ver presentación del proyecto](https://rawcdn.githack.com/Krayxzlim/SGA/main/sga-landing.html)**

---

## Tabla de contenidos

- [Descripción](#descripción)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Módulos del sistema](#módulos-del-sistema)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Roles y permisos](#roles-y-permisos)
- [Mejoras planificadas](#mejoras-planificadas)
- [Autor](#autor)

---

## Descripción

SGA centraliza la gestión operativa de talleres educativos dictados en instituciones escolares. Permite administrar colegios, talleres, usuarios y agendas desde una aplicación de escritorio, con toda la lógica de negocio expuesta a través de una API REST segura.

---

## Stack tecnológico

### Backend
| Tecnología | Uso |
|---|---|
| Java 17+ | Lenguaje principal |
| Spring Boot 3.x | Framework de aplicación |
| Spring Security | Autenticación y autorización |
| JWT (JSON Web Tokens) | Autenticación stateless |
| Hibernate / JPA | ORM y persistencia |
| MySQL | Base de datos relacional |
| Maven | Gestión de dependencias |

### Frontend
| Tecnología | Uso |
|---|---|
| JavaFX | Framework de interfaz de escritorio |
| FXML | Definición declarativa de vistas |
| Scene Builder | Diseño visual de interfaces |
| CalendarFX | Componente de calendario interactivo |

---

## Arquitectura

El sistema adopta una arquitectura de tres capas desacopladas:

```
┌─────────────────────────────────────────────────────────┐
│                  Cliente JavaFX                          │
│         (FXML · Scene Builder · CalendarFX)              │
│              Consume API REST via HTTP + JWT             │
└──────────────────────┬──────────────────────────────────┘
                       │ REST API
┌──────────────────────▼──────────────────────────────────┐
│               Backend Spring Boot                        │
│   Controller → Service → Repository                      │
│   Spring Security · JWT · Hibernate / JPA               │
└──────────────────────┬──────────────────────────────────┘
                       │ JPA
┌──────────────────────▼──────────────────────────────────┐
│                    MySQL                                 │
│   Usuarios · Talleres · Colegios · Agendas              │
└─────────────────────────────────────────────────────────┘
```

El backend expone endpoints REST protegidos por Spring Security. El cliente JavaFX se autentica con email y contraseña, recibe un token JWT y lo incluye en cada solicitud posterior. Ambas capas son independientes y pueden ejecutarse en máquinas distintas.

---

## Módulos del sistema

### 01 · Autenticación y seguridad
- Login con email y contraseña
- Generación de token JWT con rol del usuario
- Protección de endpoints según rol mediante Spring Security

### 02 · Colegios
- CRUD completo de instituciones educativas
- Registro de nombre, dirección y contacto
- Vinculación con talleres y agendas

### 03 · Talleres
- Administración de talleres educativos (nombre y descripción)
- Asignación de talleristas responsables
- Integración directa con el módulo de agenda

### 04 · Usuarios
- Gestión del ciclo de vida de usuarios (alta, modificación, baja)
- Asignación de roles: `ADMINISTRADOR`, `SUPERVISOR`, `TALLERISTA`
- Acceso restringido exclusivamente al administrador

### 05 · Agenda
- Calendario interactivo con vistas diaria y mensual (CalendarFX)
- Registro de eventos con taller, colegio y horario
- Reprogramación y modificación de actividades existentes

### 06 · Reportes
- Dashboard de métricas: talleres activos, colegios, alumnos y horas dictadas
- Visualización por taller con gráficos de barras
- Exportación de datos a Excel

---

## Requisitos previos

- **Java 17** o superior
- **Maven 3.8+**
- **MySQL 8.0+**
- **JavaFX SDK** (si no está incluido en el JDK utilizado)

---

## Instalación y ejecución

### 1. Clonar los repositorios

El proyecto está dividido en dos repositorios independientes:

```bash
# Backend
git clone https://github.com/Krayxzlim/SGA.git

# Frontend
git clone https://github.com/Krayxzlim/SGAui.git
```

### 2. Configurar la base de datos

Crear la base de datos en MySQL:

```sql
CREATE DATABASE agenda_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configurar las credenciales en `SGA/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/agenda_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Puerto
server.port=8081

# JWT
app.jwt.secret=tu_clave_secreta
app.jwt.expiration=86400000
```

### 3. Ejecutar el backend

```bash
cd SGA
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8081`.

### 4. Ejecutar el cliente JavaFX

```bash
cd SGAui
mvn javafx:run
```

> Asegurate de que el backend esté corriendo antes de iniciar el cliente. El frontend apunta por defecto a `http://localhost:8081`.

---

## Estructura del proyecto

```
# Repositorio 1
SGA/                                # Backend — API REST
├── src/main/java/
│   └── com/sga/
│       ├── controller/             # Endpoints REST
│       ├── service/                # Lógica de negocio
│       ├── repository/             # Acceso a datos (JPA)
│       ├── model/                  # Entidades JPA
│       ├── dto/                    # Objetos de transferencia
│       └── config/                 # JWT y Spring Security
└── src/main/resources/
    └── application.properties      # Configuración (DB, JWT, puerto)

# Repositorio 2
SGAui/                              # Frontend — Cliente JavaFX
├── src/main/java/
│   └── com/sga/
│       ├── controller/             # Controladores FXML
│       ├── service/                # Llamadas a la API REST
│       └── model/                  # Modelos de UI
└── src/main/resources/
    └── fxml/                       # Archivos de vista
```

---

## Roles y permisos

| Módulo | ADMINISTRADOR | SUPERVISOR | TALLERISTA |
|---|:---:|:---:|:---:|
| Autenticación | ✅ | ✅ | ✅ |
| Colegios (CRUD) | ✅ | ✅ | 👁️ solo lectura |
| Talleres (CRUD) | ✅ | ✅ | 👁️ solo lectura |
| Usuarios (CRUD) | ✅ | ❌ | ❌ |
| Agenda | ✅ | ✅ | ✅ |
| Reportes | ✅ | ✅ | ❌ |

---

## Mejoras planificadas

- 🔔 **Notificaciones** — alertas automáticas sobre talleres próximos o cambios de agenda
- 📈 **Dashboard estadístico** — métricas históricas y comparativas por período
- 🌐 **Versión web responsive** — interfaz complementaria accesible desde el navegador
- 🔍 **Auditoría de acciones** — registro de todas las operaciones por usuario
- 📄 **Gestión documental** — adjuntar archivos a talleres, colegios y usuarios
- 🔗 **Integración con servicios externos** — calendarios, email y plataformas educativas

---

## Autor

**Luciano Aufieri**

---

> 🌐 Explorá la presentación interactiva del sistema en: [rawcdn.githack.com](https://rawcdn.githack.com/Krayxzlim/SGA/main/sga-landing.html)
