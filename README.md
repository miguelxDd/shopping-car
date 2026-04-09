# Shopping Cart API

**Author:** Miguel Antonio Amaya Hernández

---

## Technologies

- Java 21
- Spring Boot 4.0
- Spring Security + JWT (jjwt 0.12)
- Spring Data JPA
- H2 Database (in-memory for development)
- Resilience4j (circuit breaker & retry)
- Swagger / OpenAPI (springdoc)
- Lombok
- Maven

## Project Structure

```
controller/     → REST Endpoints (Auth, Cart, Order, Payment, Products)
model/          → JPA Entities, DTOs and enums
repository/     → Spring Data Repositories
service/        → Business logic and integrations
security/       → JWT Filter, token service, UserDetailsService
config/         → General configuration
exception/      → Centralized error handling
mapper/         → Entity-to-DTO conversion
util/           → Utilities
```

## Getting Started

```bash
./mvnw spring-boot:run
```

The application starts at `http://localhost:8080`.

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

H2 Console: `http://localhost:8080/h2-console`

| Field     | Value                          |
|-----------|--------------------------------|
| JDBC URL  | `jdbc:h2:mem:shoppingcartdb`   |
| User      | `sa`                           |
| Password  | *(empty)*                      |

## Docker

```bash
# Dev (default)
docker compose up -d

# Prod
cp .env.example .env   # edit with actual values
SPRING_PROFILE=prod docker compose up -d
```

For production, configure in `.env`: `DB_URL`, `DB_DRIVER`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`.

Or without Compose:

```bash
docker build -t shopping-cart-api .
docker run -p 8080:8080 shopping-cart-api
```

- Multi-stage build (JDK 21 for compilation, JRE 21 for runtime)
- Non-root user inside the container
- Healthcheck via `/actuator/health`
- Memory limit 512M
- JVM configured with `-XX:MaxRAMPercentage=75.0` to respect container limits

## Tests

```bash
./mvnw test
```

Includes unit and integration tests.

## Profiles

- `dev` — In-memory H2, H2 console enabled, SQL visible in logs
- `prod` — External database via environment variables (`DB_URL`, `DB_DRIVER`, `DB_USERNAME`, `DB_PASSWORD`)

## External API

Consumes the [Fake Store API](https://fakestoreapi.com/products) to fetch products, with circuit breaker and retry configured via Resilience4j.

## Postman Collection

Import the `Shopping-Cart-API.postman_collection.json` file included in the project root.

---

# Shopping Cart API (Español)

API REST para gestión de carrito de compras, órdenes y pagos.

**Autor:** Miguel Antonio Amaya Hernández

---

## Tecnologías

- Java 21
- Spring Boot 4.0
- Spring Security + JWT (jjwt 0.12)
- Spring Data JPA
- H2 Database (en memoria para desarrollo)
- Resilience4j (circuit breaker y retry)
- Swagger / OpenAPI (springdoc)
- Lombok
- Maven

## Estructura del proyecto

```
controller/     → Endpoints REST (Auth, Cart, Order, Payment, Products)
model/          → Entidades JPA, DTOs y enums
repository/     → Repositorios Spring Data
service/        → Lógica de negocio e integraciones
security/       → Filtro JWT, servicio de tokens, UserDetailsService
config/         → Configuración general
exception/      → Manejo centralizado de errores
mapper/         → Conversión entre entidades y DTOs
util/           → Utilidades
```

## Cómo correr

```bash
./mvnw spring-boot:run
```

La app levanta en `http://localhost:8080`.

Swagger UI disponible en: `http://localhost:8080/swagger-ui.html`

Consola H2: `http://localhost:8080/h2-console`

| Campo     | Valor                          |
|-----------|--------------------------------|
| JDBC URL  | `jdbc:h2:mem:shoppingcartdb`   |
| User      | `sa`                           |
| Password  | *(vacío)*                      |

## Docker

```bash
# Dev (por defecto)
docker compose up -d

# Prod
cp .env.example .env   # editar con los valores reales
SPRING_PROFILE=prod docker compose up -d
```

En producción se requiere configurar en `.env`: `DB_URL`, `DB_DRIVER`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`.

O sin Compose:

```bash
docker build -t shopping-cart-api .
docker run -p 8080:8080 shopping-cart-api
```

- Multi-stage build (JDK 21 para compilar, JRE 21 para correr)
- Usuario no-root dentro del contenedor
- Healthcheck via `/actuator/health`
- Límite de memoria 512M
- JVM configurada con `-XX:MaxRAMPercentage=75.0` para respetar los límites del contenedor

## Tests

```bash
./mvnw test
```

Incluye tests unitarios y de integración.

## Perfiles

- `dev` — H2 en memoria, consola H2 habilitada, SQL visible en logs
- `prod` — Base de datos externa vía variables de entorno (`DB_URL`, `DB_DRIVER`, `DB_USERNAME`, `DB_PASSWORD`)

## API externa

Se consume la [Fake Store API](https://fakestoreapi.com/products) para obtener productos, con circuit breaker y retry configurados via Resilience4j.

## Colección Postman

Importar el archivo `Shopping-Cart-API.postman_collection.json` incluido en la raíz del proyecto.
