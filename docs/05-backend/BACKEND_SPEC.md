# Backend Spec

## Stack

- Java 17 recommended
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 local database
- Lombok optional
- Validation

## Package Structure

```text
com.campio
  domain
    user
    opportunity
    saved
    application
    community
    mentor
  global
    config
    exception
    response
    security
```

## Rules

- Controller handles HTTP.
- Service handles business logic.
- Repository handles database access.
- Use DTOs.
- Entity should not be exposed directly if avoidable.
- Do not seed sample application data.
- Bootstrap only the configured admin account from environment variables.
- Populate opportunities through ingestion import, API adapters, or RSS adapters.
