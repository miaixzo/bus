/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.boot.environment;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor;
import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.IterableConfigurationPropertySource;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.aot.AbstractAotProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.registry.RegistryEndpoint;
import org.miaixz.bus.cortex.setting.secret.SealingSecretCodec;
import org.miaixz.bus.logger.Logger;

/**
 * Loads the application's published Cortex configuration during Spring Boot environment preparation.
 * <p>
 * The processor reads startup settings after Spring Boot has loaded local Config Data. It selects one remote release or
 * fallback file before logging configuration and application bean binding begin.
 */
public class RemoteEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * Name of the selected startup property source in the Spring environment.
     */
    private static final String SOURCE = "bus.cortex.config.snapshot";
    /**
     * Namespace for the local parameters used to load startup configuration.
     */
    private static final String PREFIX = "bus.cortex.config";
    /**
     * Namespaces reserved for local startup parameters and Spring Boot startup controls.
     */
    private static final List<String> BOOTSTRAP = List.of(
            PREFIX,
            RegistryEndpoint.PATH_PROPERTY,
            "register.url",
            "gateway.url",
            "spring.config",
            "spring.profiles",
            "spring.main");
    /**
     * Revalidates property sources and installs binding validation after configuration sources are added.
     */
    private static final ApplicationContextInitializer<ConfigurableApplicationContext> VERIFY_BINDING = context -> context
            .addBeanFactoryPostProcessor(factory -> {
                var environment = context.getEnvironment();
                if (environment.getPropertySources().get(SOURCE) instanceof Source selected) {
                    validateSources(environment, selected);
                    factory.registerSingleton("busCortexConfigBindingAdvisor", selected.advisor());
                }
            });

    /**
     * Creates the processor registered through Spring Boot's factory discovery.
     */
    public RemoteEnvironmentPostProcessor() {
        // No initialization required.
    }

    /**
     * Runs immediately after Spring Boot processes local Config Data.
     *
     * @return environment post-processor order
     */
    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    /**
     * Loads the selected snapshot when a Cortex gateway is configured and registers binding validation.
     * <p>
     * The processor performs runtime reads only and reuses a snapshot already installed in the environment.
     *
     * @param environment Spring environment containing the local startup parameters
     * @param application Spring application that owns the startup context
     * @throws IllegalStateException if startup configuration cannot be loaded or conflicts with another source
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (Boolean.getBoolean(AbstractAotProcessor.AOT_PROCESSING)
                || environment.getPropertySources().contains(SOURCE)) {
            return;
        }
        Binder binder = Binder.get(environment);
        String app = value(binder, "app.id"), token = value(binder, "token");
        if (app.isBlank() && token.isBlank()) {
            return;
        }
        Options options = Options.read(binder, environment.getProperty(RegistryEndpoint.REGISTER_URL), app, token);
        Source selected;
        try {
            selected = load(options);
        } catch (IOException error) {
            throw new IllegalStateException("Could not load Cortex startup configuration", error);
        }
        validateSources(environment, selected);
        environment.getPropertySources().addFirst(selected);
        application.addInitializers(VERIFY_BINDING);
        Logger.info(
                selected.fallback == null ? "Cortex startup configuration source=REMOTE revision=" + selected.revision
                        : "Cortex startup configuration source=FILE reason=" + selected.fallback);
    }

    /**
     * Reports whether the selected startup snapshot supplies a property required by the application.
     *
     * @param environment Spring environment that may contain a startup snapshot
     * @param name        canonical Spring Boot property name
     * @return {@code true} when the selected snapshot declares the property
     */
    public static boolean declares(Environment environment, String name) {
        return environment instanceof ConfigurableEnvironment configurable
                && configurable.getPropertySources().get(SOURCE) instanceof Source source
                && source.declares(ConfigurationPropertyName.of(name));
    }

    /**
     * Matches a property name against complete namespace segments.
     *
     * @param name     canonical property name
     * @param prefixes namespace roots
     * @return {@code true} when the name is a namespace root or one of its descendants
     */
    private static boolean belongs(String name, List<String> prefixes) {
        return prefixes.stream().anyMatch(
                prefix -> name.equals(prefix) || name.startsWith(prefix + Symbol.DOT)
                        || name.startsWith(prefix + Symbol.BRACKET_LEFT));
    }

    /**
     * Reads one local Cortex startup parameter.
     *
     * @param binder binder for the local Spring environment
     * @param key    property name relative to the Cortex startup namespace
     * @return configured value, or an empty string when absent
     */
    private static String value(Binder binder, String key) {
        return binder.bind(PREFIX + Symbol.DOT + key, String.class).orElse(Normal.EMPTY);
    }

    /**
     * Holds the validated local settings for selecting a startup snapshot.
     *
     * @param endpoint complete Vortex HTTPS registration URL
     * @param app      registered application code
     * @param token    application reader credential
     * @param fallback optional fallback file
     * @param keys     optional configuration delivery key file
     * @param ca       optional Vortex trust root file
     */
    private record Options(URI endpoint, String app, String token, Path fallback, Path keys, Path ca) {

        /**
         * Validates the endpoint, application identity, and optional file locations.
         *
         * @param binder     binder for local startup settings
         * @param configured shared registration URL
         * @param app        registered application code
         * @param token      application reader credential
         * @return validated startup settings
         * @throws IllegalArgumentException if the origin or a file location cannot be parsed
         * @throws IllegalStateException    if a setting violates the startup configuration contract
         */
        static Options read(Binder binder, String configured, String app, String token) {
            URI endpoint = RegistryEndpoint.parse(configured);
            if (!"https".equalsIgnoreCase(endpoint.getScheme()) || !SealingSecretCodec.validCoordinate(app)
                    || !SealingSecretCodec.validCredential(token)) {
                throw new IllegalStateException(
                        "Cortex startup configuration requires an HTTPS registration URL, a registered app ID and a reader token");
            }
            return new Options(endpoint, app, token, path(value(binder, "fallback.location"), true),
                    path(value(binder, "secret.key.file"), false), path(value(binder, "ca.file"), false));
        }

        /**
         * Resolves an optional absolute file location.
         *
         * @param value   configured location
         * @param fileUri whether the location must be a {@code file:} URI
         * @return file path, or {@code null} when the setting is empty
         * @throws IllegalStateException if the required URI scheme is missing or the path is relative
         */
        private static Path path(String value, boolean fileUri) {
            if (value.isBlank()) {
                return null;
            }
            if (fileUri && !value.startsWith("file:")) {
                throw new IllegalStateException("SDC fallback must be an absolute file URI");
            }
            Path path = fileUri ? Path.of(URI.create(value)) : Path.of(value);
            if (!path.isAbsolute()) {
                throw new IllegalStateException("SDC file locations must be absolute");
            }
            return path;
        }

    }

    /**
     * Selects a published release, or reads the fallback file when the fetcher reports unavailability.
     *
     * @param options validated startup settings
     * @return property source containing the selected snapshot
     * @throws IOException           if local files needed for startup cannot be read
     * @throws IllegalStateException if the selected configuration or gateway response is invalid
     */
    private static Source load(Options options) throws IOException {
        var remote = new EnvironmentConfigurationFetcher(options.endpoint(), options.app(), options.token(),
                options.keys(), options.ca()).fetch();
        var values = new LinkedHashMap<String, Object>();
        if (remote.unavailable() != null) {
            fallback(options.fallback(), remote.unavailable(), values);
        } else {
            for (Object item : remote.configs().values()) {
                flatten(Normal.EMPTY, item, values);
            }
        }
        return new Source(values, validate(values), remote.revision(), remote.unavailable());
    }

    /**
     * Reads one YAML or properties document into the selected startup values.
     *
     * @param path   configured fallback file
     * @param reason reason returned by the remote fetcher
     * @param values destination for the file's properties
     * @throws IOException           if file size or contents cannot be read
     * @throws IllegalStateException if the file is missing, exceeds the size limit, or has an invalid structure
     */
    private static void fallback(Path path, String reason, Map<String, Object> values) throws IOException {
        if (path == null || !Files.isRegularFile(path) || Files.size(path) > SealingSecretCodec.MAX_BYTES) {
            throw new IllegalStateException("SDC " + reason + "; fallback configuration is missing or invalid");
        }
        String name = path.getFileName().toString();
        var loader = switch (name.substring(name.lastIndexOf(Symbol.C_DOT) + 1)) {
            case "properties" -> new PropertiesPropertySourceLoader();
            case "yaml", "yml" -> new YamlPropertySourceLoader();
            default -> throw new IllegalStateException("SDC fallback requires YAML or properties");
        };
        var sources = loader.load("sdc-fallback", new FileSystemResource(path));
        if (sources.size() != 1 || !(sources.getFirst() instanceof EnumerablePropertySource<?> source)) {
            throw new IllegalStateException("SDC fallback requires one configuration document");
        }
        for (String key : source.getPropertyNames()) {
            Object value = source.getProperty(key);
            if (value == null) {
                throw new IllegalStateException("SDC fallback contains an empty property: " + key);
            }
            values.put(key, value);
        }
    }

    /**
     * Expands JSON objects and lists into dotted and indexed Spring property names.
     *
     * @param parent flattened path to the current value, or an empty string at the item root
     * @param value  JSON value represented as a Java object
     * @param target destination shared by all items in the release
     * @throws IllegalStateException if a property name is blank or a leaf is null or already defined
     */
    private static void flatten(String parent, Object value, Map<String, Object> target) {
        if (value instanceof Map<?, ?> map) {
            map.forEach((key, child) -> {
                String name = String.valueOf(key);
                if (name.isBlank()) {
                    throw new IllegalStateException("SDC configuration contains an invalid property name");
                }
                flatten(
                        parent.isEmpty() || name.startsWith(Symbol.BRACKET_LEFT) ? parent + name
                                : parent + Symbol.DOT + name,
                        child,
                        target);
            });
        } else if (value instanceof List<?> list) {
            for (int index = 0; index < list.size(); index++) {
                flatten(parent + Symbol.BRACKET_LEFT + index + Symbol.BRACKET_RIGHT, list.get(index), target);
            }
        } else if (value == null || target.putIfAbsent(parent, value) != null) {
            throw new IllegalStateException("SDC configuration contains a duplicate or null property");
        }
    }

    /**
     * Canonicalizes property names and checks the snapshot for duplicate and reserved keys.
     *
     * @param values flattened snapshot
     * @return immutable set of canonical property names
     * @throws IllegalStateException if the snapshot is empty or contains an invalid, duplicate, or reserved name
     */
    private static Set<ConfigurationPropertyName> validate(Map<String, Object> values) {
        Set<ConfigurationPropertyName> canonical = new LinkedHashSet<>();
        if (values.isEmpty()) {
            throw new IllegalStateException("SDC startup configuration is empty");
        }
        for (String property : values.keySet()) {
            var name = ConfigurationPropertyName.adapt(property, Symbol.C_DOT);
            if (name.isEmpty() || !canonical.add(name) || belongs(name.toString(), BOOTSTRAP)) {
                throw new IllegalStateException("Invalid, duplicate or bootstrap SDC property: " + name);
            }
        }
        return Set.copyOf(canonical);
    }

    /**
     * Adds release and fallback diagnostics to the selected startup property source.
     */
    private static final class Source extends EnvironmentSnapshotPropertySource {

        /**
         * Published release identifier, or {@code null} for a file snapshot.
         */
        private final String revision;
        /**
         * Remote unavailability reason, or {@code null} for a published release.
         */
        private final String fallback;

        /**
         * Creates the property source for the selected startup snapshot.
         *
         * @param values   flattened property values
         * @param names    canonical property names
         * @param revision published release identifier, or {@code null} for a file snapshot
         * @param fallback remote unavailability reason, or {@code null} for a published release
         */
        Source(Map<String, Object> values, Set<ConfigurationPropertyName> names, String revision, String fallback) {
            super(SOURCE, values, names);
            this.revision = revision;
            this.fallback = fallback;
        }

        /**
         * Ensures a configuration object owned by the snapshot binds from that source alone.
         *
         * @return advisor for Spring Boot configuration-property binding
         */
        ConfigurationPropertiesBindHandlerAdvisor advisor() {
            return parent -> new AbstractBindHandler(parent) {

                /**
                 * Whether the snapshot owns the current top-level binding.
                 */
                private boolean managed;

                @Override
                public <T> Bindable<T> onStart(
                        ConfigurationPropertyName name,
                        Bindable<T> target,
                        BindContext context) {
                    if (context.getDepth() == 0) {
                        managed = owns(name);
                    }
                    return super.onStart(name, target, context);
                }

                @Override
                public Object onSuccess(
                        ConfigurationPropertyName name,
                        Bindable<?> target,
                        BindContext context,
                        Object result) {
                    var property = context.getConfigurationProperty();
                    if (managed && property != null && property.getName().equals(name) && property.getSource() != null
                            && property.getSource().getUnderlyingSource() != Source.this) {
                        throw new IllegalStateException("SDC binding mixes configuration sources at " + name);
                    }
                    return super.onSuccess(name, target, context, result);
                }

            };
        }

    }

    /**
     * Checks enumerable Spring property sources for other declarations of snapshot properties.
     *
     * @param environment environment that supplies the participating property sources
     * @param selected    startup snapshot
     * @throws IllegalStateException if another source declares a property supplied by the snapshot
     */
    private static void validateSources(ConfigurableEnvironment environment, Source selected) {
        for (var candidate : ConfigurationPropertySources.get(environment)) {
            if (candidate.getUnderlyingSource() == selected) {
                continue;
            }
            if (candidate instanceof IterableConfigurationPropertySource iterable) {
                for (var key : iterable) {
                    if (selected.declares(key)) {
                        throw new IllegalStateException("Cortex configuration has another source for " + key);
                    }
                }
            }
        }
    }

}
