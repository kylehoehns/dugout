# dugout agent instructions

## Commands
- Build: `./gradlew build`
- Test (all): `./gradlew test`
- Test (single): `./gradlew test --tests "com.kylehoehns.dugout.player.PlayerApiIT"`
- Run locally: `./gradlew bootRun`

## Rules (most-violated first)
- This is a **Gradle** project — build and test with `./gradlew`, never Maven (`mvn`).
- Do not add Lombok; use plain Java.
- JPA entities are plain classes (a record cannot be an `@Entity`); use records for DTOs.
