# Crowdsourced-Waste-Collection-Recycling-System
A full-stack system for urban waste collection and recycling management in Vietnam. Built with Spring Boot and ReactJS, integrated with OpenStreetMap for GPS-based reporting, collection coordination, and transparent monitoring to support mandatory waste segregation regulations.

## Requirements

- JDK 24 (matches `pom.xml` property `java.version`)

## Build

Use the Maven Wrapper included in the repo.

```powershell
./mvnw.cmd clean test
```

## Run (Backend)

```powershell
./mvnw.cmd spring-boot:run
```

## Troubleshooting

### `java: cannot access CitizenRewardHistoryResponse (class file not found)`

This usually indicates an IDE/classpath cache or Maven import issue (the class exists under `src/main/java`).

- Reimport the Maven project (IntelliJ: Maven tool window → Reload; Eclipse/VS Code: refresh Maven).
- Clean and rebuild from the repo root:

```powershell
./mvnw.cmd clean compile
```

- Ensure the IDE uses the same JDK as the project (JDK 24).
- If your IDE builds using its own compiler, enable annotation processing (required by Lombok/MapStruct).
