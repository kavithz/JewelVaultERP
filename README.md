# JewelVaultERP

A full-stack Jewellery Enterprise Resource Planning system.

## Stack

### Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway

### Frontend
- Angular
- TypeScript

### Deployment
- Vercel
- Neon PostgreSQL
- Render

## Deployment Configuration

Deploy the Angular project from `frontend/web` to Vercel. Set the public build
environment variable `API_BASE_URL` to the Spring Boot API origin, including
`https://` and without a trailing slash. The production bundle uses that value;
if it is unset, requests use the frontend origin. The Vercel configuration
serves the Angular SPA routes from `index.html`.

Build the backend from the `backend` directory with its Dockerfile. Configure
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_ISSUER`,
`JWT_EXPIRATION_SECONDS`, `APP_CORS_ALLOWED_ORIGINS`, and `SERVER_PORT` in the
backend host's environment settings. Use the exact HTTPS Vercel origin for
`APP_CORS_ALLOWED_ORIGINS`; store database and JWT secrets only in the provider.
`JWT_SECRET` must be Base64-encoded key material of at least 32 bytes. Flyway
applies the existing ordered migrations at startup, and Hibernate validates the
resulting schema. Back up an existing database before its first deployment.

Preview Mode uses local sample data and does not call the backend. Real Mode
uses `API_BASE_URL` and the authenticated backend API.
