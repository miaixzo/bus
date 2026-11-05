# Bus Native

Third-party GraalVM reachability metadata and initialization settings used by Bus integrations.
This is a resource-only JAR: it contains no Java classes and declares no runtime dependencies.

- Bus-owned registrations remain in their original `org.miaixz/<module>` directories.
- Third-party registrations and indexes live here under `META-INF/native-image/<group>/<artifact>/<version>`.
  Version-independent Netty and SLF4J package initialization settings use their artifact directories without a version.
- OpenCV metadata stays in `bus-opencv`; that module does not depend on `bus-native`.
- Bouncy Castle integration metadata uses `org.bouncycastle/bcpkix-jdk18on/1.85`, matching the managed dependency.
- Integration modules requiring these settings depend on this JAR directly. `bus-core` does not depend on it. `bus-all` embeds these resources.
  Applications using standard Bus modules do not need to add this dependency explicitly.
- Existing registration conditions and upstream metadata deduplication are preserved. This JAR supplements the
  official metadata repository; enable that repository in the application's Native Build Tools configuration.
- Initialization settings are preserved from the integration modules, including Bouncy Castle, Fastjson2, Netty,
  SLF4J and Spring Boot Loader. Applications must still validate their specific Native Image build.

Maintain a third-party entry here when an integration needs coverage beyond the selected upstream metadata.
Keep its version index and reachability conditions together; do not copy the same entries back into Bus modules.
