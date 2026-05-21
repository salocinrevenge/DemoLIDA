# Repository Guidelines

## Project Structure & Module Organization

DemoLIDA is a Gradle Java application that demonstrates a LIDA agent in the WorldServer3D environment. Main source code lives in `src/main/java`: `Run.java` starts the agent, `modules/` contains environment and sensory memory code, `detectors/` contains LIDA detector implementations, and `guipanels/` contains GUI integration. Runtime configuration is in `src/main/resources/configs/`, including `Agent.xml`, factory data, GUI properties, logging, and XML schemas. Local JAR dependencies are kept in `lib/`. The `configs/` and `bin/main/configs/` directories mirror configuration/output files; prefer editing `src/main/resources/configs/` for source changes.

## Build, Test, and Development Commands

- `./ws3d.sh`: starts the WS3D Docker environment required by the demo.
- `./gradlew run`: runs the Java application with main class `Run`; start WS3D first.
- `./gradlew build`: compiles sources and creates the application artifacts.
- `./gradlew jar`: builds a full runnable JAR with runtime dependencies bundled.
- `./gradlew test`: runs the Gradle test task.

Use `./gradlew.bat` equivalents on Windows.

## Coding Style & Naming Conventions

Use standard Java conventions: four-space indentation, `PascalCase` classes, `camelCase` methods and fields, and `UPPER_SNAKE_CASE` constants. Keep package names lowercase and aligned with the current folders (`modules`, `detectors`, `guipanels`). Prefer small, focused detector classes and keep WS3D/LIDA integration logic inside the module layer unless a GUI panel specifically needs it. Comments may be in English or Portuguese when matching nearby code, but keep new comments short and useful.

## Testing Guidelines

There is currently no `src/test` tree, although Gradle is configured to use JUnit Platform. Add tests under `src/test/java` when introducing logic that can be exercised without WS3D. Name test classes after the subject, for example `FoodDetectorTest` or `EnvironmentTest`. For behavior that depends on Docker or the WS3D server, document manual verification steps in the pull request and run `./ws3d.sh` plus `./gradlew run`.

## Commit & Pull Request Guidelines

Recent commits use short, imperative summaries, often in Portuguese, such as `vai ate delivery spot` or `Adicionado o deliveryspot`. Keep commit messages concise and describe the behavior changed. Pull requests should include a brief description, relevant issue or course task reference, commands run (`./gradlew build`, `./gradlew test`, manual WS3D run), and screenshots or terminal notes when GUI or agent behavior changes.

## Security & Configuration Tips

Do not commit generated build output or local environment changes. Keep third-party JAR updates intentional and reflected in `build.gradle`. Avoid hardcoding machine-specific paths; configuration should stay in `src/main/resources/configs/`.
