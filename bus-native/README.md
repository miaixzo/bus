# Bus Native

Third-party GraalVM reachability metadata and initialization settings used by Bus integrations.
This is a resource-only JAR: it contains no Java classes and declares no runtime dependencies.

- Bus-owned registrations remain in their original `org.miaixz/<module>` directories.
  Third-party reflection, proxy, JNI and framework resource registrations belong here under the corresponding library.
  Third-party parameter types in Bus method signatures remain part of the Bus method registration.
  Bus SPI files, auto-configuration lists and JDK settings remain in the components that use them.
- Third-party registrations live under `META-INF/native-image/<group>/<artifact>/<baseline-version>/`,
  using the same version-line baseline convention as Bus components rather than separate patch-version directories.
  The baseline must be the first stable release actually published to Maven Central for that version line,
  verified against the coordinate's `maven-metadata.xml` and version POM. Never derive it by zeroing the patch
  number or appending `.0`: POI uses `5.5.0`, while Bouncy Castle uses its official version name `1.85`.
  Every artifact root must contain `index.json`, pointing to the baseline through `metadata-version`.
  Every version directory must have a corresponding index entry; do not retain unindexed settings or empty directories.
  The index lists specific supported versions in `tested-versions`.
  The list must include the baseline and be sorted in ascending version order.
  Add and validate new patch versions in that list; publication alone does not establish compatibility.
  Logback, Netty and SLF4J package initialization settings also use version-line baseline directories and indexes
  declaring supported versions. Artifact roots contain only `index.json`, with initialization settings below the baseline.
  Netty uses the full official version name `4.2.0.Final`, retaining the `.Final` suffix.
- JDK registrations belong to the Bus components that use them; this module does not maintain a separate `jdk` directory.
  Generic reflection interface registrations are merged into `bus-core` under
  `org.miaixz/bus-core/8.8.0/reachability-metadata.json`. AWT, font, ImageIO and Java2D JNI/resource registrations
  needed by Office are merged into `bus-office` under `org.miaixz/bus-office/8.8.0/reachability-metadata.json`,
  retaining their existing reachability conditions.
- Jackson 2 default-converter constructors, Temporal service-client proxies and shaded gRPC registrations belong to their
  dependency directories. This module also owns Logback, SLF4J and shaded gRPC class initialization settings.
  Standard Logback configuration resources live under `ch.qos.logback/logback-classic/1.6.0`;
  `logback-spring.*` resources live under `org.springframework.boot/spring-boot/4.1.0`.
  Application types, enums and composite proxies containing application interfaces remain in the application.
- JNI registrations for Bus-owned OpenCV bindings and `lib/**` resources bundled with that component stay in `bus-opencv`;
  that module does not depend on `bus-native`.
- Bouncy Castle provider reflection and initialization metadata uses `org.bouncycastle/bcprov-jdk18on/1.85`,
  matching the actual class owner. The managed `bcpkix-jdk18on` dependency brings in this library transitively;
  version `1.85` is declared in `tested-versions`.
- Integration modules requiring these settings depend on this JAR directly. `bus-core` does not depend on it. `bus-all` embeds these resources.
  Applications using standard Bus modules do not need to add this dependency explicitly.
- Existing registration conditions and upstream metadata deduplication are preserved. This JAR supplements the
  official metadata repository; enable that repository in the application's Native Build Tools configuration.
- Initialization settings are preserved from the integration modules, including Bouncy Castle, Fastjson2, Netty,
  SLF4J and Spring Boot Loader. Applications must still validate their specific Native Image build.

Maintain a third-party entry here when an integration needs coverage beyond the selected upstream metadata.
Keep its version index and reachability conditions together; do not copy the same entries back into Bus modules.

Shared POI full/lite schema declarations are maintained once in the `poi-ooxml-lite` metadata;
`poi-ooxml-full` contains only its additional schema declarations. This resource JAR always includes both files.
The ImageIO reader/writer SPI rules in `bus-image` and `bus-office` are intentionally independent, because either
component can be consumed on its own. Generic `Object`/`Enum` reflection and Bus core declarations belong to `bus-core`.

Run `node .github/scripts/native.cjs` from the repository root to check every component's ownership,
duplicates and indexes. The audit reports these two intentional ImageIO SPI overlaps separately.
After a complete clean build, `node .github/scripts/artifact.cjs` compares compiled classes and native
metadata with the component JARs, verifies the effective `bus-all` coverage, and checks installed artifact hashes.
`bus-opencv` remains a separate JNI artifact, as declared by the aggregate's dependency graph.
