# Bus Preset

[简体中文](README_CN.md)

Bus Preset packages GraalVM Native Image configuration for the third-party libraries used by Bus. It supplies reachability metadata for reflection, proxies, JNI and resources, along with class initialization settings.

| Maven coordinate | Java module | Runtime dependencies |
| --- | --- | --- |
| `org.miaixz:bus-preset` | `bus.preset` | None |

## Use in an application

Bus integration modules that need these settings already depend on `bus-preset`. The `bus-all` JAR embeds the same resources. Applications using either entry point receive the configuration with their Bus dependencies.

To add the resource JAR directly, import [`bus-bom`](../bus-bom/pom.xml) and declare this dependency. Without the BOM, set its version to the Bus version used by your application.

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-preset</artifactId>
</dependency>
```

Keep the upstream reachability metadata repository enabled in Native Build Tools. Bus Preset supplies the additional registrations needed by Bus integrations. Build and run your application's native executable to verify its own configuration. [GraalVM Reachability Metadata](https://www.graalvm.org/latest/reference-manual/native-image/metadata/)

## Where configuration belongs

| Configuration | Owner |
| --- | --- |
| Supplemental third-party reflection, proxies, JNI, resource declarations and initialization settings | `bus-preset`, under the owning library's Maven coordinate |
| Bus types and methods, Bus SPI files, auto-configuration lists and JDK registrations used by Bus | The Bus component that uses them |
| Application types, enums and proxy combinations involving application interfaces | The application |

A Bus method registration stays with its declaring component even when its signature contains third-party parameter types. Shared `Object` and `Enum` reflection registrations live in [`bus-core`](../bus-core/). Office-specific AWT, font and ImageIO configuration lives in [`bus-office`](../bus-office/). [`bus-opencv`](../bus-opencv/) ships its own JNI bindings and native library resources as an independent artifact.

## Find a library's configuration

Open [`src/main/resources/META-INF/native-image`](src/main/resources/META-INF/native-image/) and follow the library's Maven group and artifact IDs. For example, Netty's package initialization settings use this layout.

```text
META-INF/native-image/
└── io.netty/
    └── netty-common/
        ├── index.json
        └── 4.2.0.Final/
            └── native-image.properties
```

| Index field | Meaning | Netty example |
| --- | --- | --- |
| `metadata-version` | Directory containing configuration shared by the release line | `4.2.0.Final` |
| `tested-versions` | Library versions validated against that configuration | `4.2.0.Final`, `4.2.17.Final` |

The baseline uses the official release name. Netty retains `.Final`, and Bouncy Castle uses `1.85`. [`Netty index`](src/main/resources/META-INF/native-image/io.netty/netty-common/index.json) · [`Bouncy Castle index`](src/main/resources/META-INF/native-image/org.bouncycastle/bcprov-jdk18on/index.json)

## Maintain an entry

1. **Identify the library that owns the target.** Check the JAR containing the registered class or resource. For example, Bouncy Castle provider registrations belong to `bcprov-jdk18on`, which the managed `bcpkix-jdk18on` dependency brings in transitively.
2. **Keep the supplement small.** Compare with upstream metadata and add only the registrations Bus needs. Preserve reachability conditions and the order of method parameters and proxy interfaces.
3. **Use a published baseline.** Verify the first stable release for the version line using Maven Central's `maven-metadata.xml` and version POM. Use the exact release name for the directory and its `metadata-version` entry.
4. **Record verified versions.** Include the baseline in `tested-versions` and keep the list in ascending order. Add a patch version after validating it. Each nonempty version directory needs a corresponding entry in the artifact's `index.json`.

POI schema registrations shared by the full and lite artifacts live in `poi-ooxml-lite`. `poi-ooxml-full` adds only its extra types. The `bus-image` and `bus-office` ImageIO reader and writer SPI entries are intentionally separate because each component can run independently.

## Run the checks

Run these commands from the repository root.

| Command | Checks |
| --- | --- |
| `node .github/scripts/native.cjs` | Source configuration ownership, duplicate entries and version indexes. The two shared ImageIO SPI entries are reported separately. |
| `node .github/scripts/artifact.cjs` | After a full `clean install`, compares compiled classes, source JARs, packaged metadata and installed JAR hashes, and checks coverage in `bus-all`. |

CI runs both checks after installing all Bus modules. A failure blocks the downstream release jobs. [Build action](../.github/actions/build-project/action.yml) · [Maven Central workflow](../.github/workflows/maven-central.yml)
