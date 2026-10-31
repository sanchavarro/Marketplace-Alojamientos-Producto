# Marketplace de Alojamientos — Inversiones LR

Proyecto de Ingeniería de Software · Universidad El Bosque.

Plataforma web donde el **anfitrión** publica alojamientos, el **huésped** busca, cotiza y reserva pagando en línea,
y el **administrador** modera publicaciones, gestiona usuarios y consulta reportes.

## Equipo

| Integrante | Rol |
|---|---|
| Santiago Chavarro | Gerente de proyecto / Scrum Master |
| Andrés Jarro | Analista |
| Carlos Luna | QA |

## Tecnologías

- Backend: Java 21, Spring Boot 3.5, Spring Data JPA, Spring Security + JWT
- Base de datos: PostgreSQL 17
- Pagos: modo simulado por defecto, Stripe en modo pruebas
- CI: GitHub Actions

## Cómo ejecutarlo

```bash
psql postgres -f docs/base_de_datos.sql   # crea las bases y el usuario
cd backend
./mvnw spring-boot:run                    # API en http://localhost:8080
./mvnw test                               # pruebas
```

Más detalle (endpoints por historia, ADR, patrones, pagos de prueba): [docs/GUIA_BACKEND.md](docs/GUIA_BACKEND.md).
Colección de Postman: `docs/Marketplace_LR.postman_collection.json`.
