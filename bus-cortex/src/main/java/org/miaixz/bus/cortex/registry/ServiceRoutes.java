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
package org.miaixz.bus.cortex.registry;

import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Instance;

/**
 * Combines a pre-maintained API contract with independently leased application service instances. No database, local
 * address default or registration-side API definition is consulted by this adapter.
 *
 * @author Kimi Liu
 */
public final class ServiceRoutes {

    /**
     * Matches a named path placeholder, such as the code in a registered public slug route.
     */
    private static final Pattern PATH_VARIABLE = Pattern
            .compile(Pattern.quote(Symbol.BRACE_LEFT) + "[A-Za-z][A-Za-z0-9_]*" + Pattern.quote(Symbol.BRACE_RIGHT));

    /**
     * Prevents construction of the runtime projection utility.
     */
    private ServiceRoutes() {
        // No initialization required.
    }

    /**
     * Builds an address-free definition with a complete eligible pool and an instance-aware modification watermark.
     * Retired instances are retained as revision evidence; expiry changes the projection without a database write.
     *
     * @param definition authoritative API contract
     * @param instances  all service announcements for its application, including tombstones
     * @param now        server evaluation time in milliseconds
     * @return detached runtime projection preserving the API ID, scope, method, version and verb
     */
    public static Assets project(Assets definition, Collection<? extends Instance> instances, long now) {
        Assets result = BeanKit.toBean(definition, Assets.class);
        long revision = Objects.requireNonNull(definition.getModified(), "API modification time required");
        for (Instance instance : instances) {
            if (!Objects.equals(definition.getApp_id(), instance.getApp_id()))
                throw new IllegalArgumentException("Instance belongs to another application");
            revision = Math.max(revision, instance.getModified() == null ? Normal._0 : instance.getModified());
            var health = instance.healthView();
            if (health.getBeat() != null && health.getLease() != null) {
                long expires = Math.addExact(health.getBeat(), health.getLease() * (long) Normal._1000);
                if (expires <= now)
                    revision = Math.max(revision, expires);
            }
        }
        result.setModified(revision);
        result.setInstances(
                instances.stream().filter(instance -> eligible(definition, instance, now))
                        .sorted(Comparator.comparing(Instance::getId))
                        .map(instance -> BeanKit.toBean(instance, Instance.class)).toList());
        return result;
    }

    /**
     * Checks both lifecycle and lease on every use, including uses of an already cached gateway snapshot.
     *
     * @param instance service announcement
     * @param now      evaluation time
     * @return whether the process is ready, leased and enabled for traffic
     */
    public static boolean eligible(Instance instance, long now) {
        return instance != null && Builder.INSTANCE_KIND_SERVICE.equals(instance.getKind()) && instance.isHealthyNow()
                && instance.isOnline(now) && instance.getWeight() != null && instance.getWeight() > Normal._0;
    }

    /**
     * Checks whether a currently leased service address belongs to the application that owns the API.
     *
     * @param definition maintained API contract
     * @param instance   proposed runtime listener
     * @param now        server evaluation time
     * @return whether the listener may serve this API
     */
    public static boolean eligible(Assets definition, Instance instance, long now) {
        return definition != null && eligible(instance, now)
                && Objects.equals(definition.getApp_id(), instance.getApp_id());
    }

    /**
     * Binds one request to one current process while leaving the shared definition and pool untouched.
     *
     * @param definition runtime definition with an explicit instance pool
     * @param instance   selected process from that pool
     * @param now        evaluation time
     * @return request-local route containing only the selected instance's runtime address
     */
    public static Assets bind(Assets definition, Instance instance, long now) {
        if (!eligible(definition, instance, now))
            throw new IllegalArgumentException("Service instance is not eligible for this API");
        validate(instance);
        validateOperation(definition.getUrl());
        Assets route = BeanKit.toBean(definition, Assets.class);
        route.setInstances(List.of(BeanKit.toBean(instance, Instance.class)));
        return route;
    }

    /**
     * Resolves the selected request's origin and deployment prefix from its sole Instance reference. No address is
     * copied into the API definition. Explicit absolute URLs remain available to non-registry protocol routes.
     *
     * @param asset request-local bound API or an explicitly configured absolute protocol route
     * @return validated origin and deployment prefix
     * @throws IllegalStateException when no unique current runtime selection or explicit absolute target exists
     */
    public static URI resolve(Assets asset) {
        Objects.requireNonNull(asset, "Route definition required");
        if (asset.getInstances() == null) {
            URI target = URI.create(Objects.requireNonNull(asset.getUrl(), "Explicit target required"));
            if (!target.isAbsolute() || target.getRawAuthority() == null || target.getUserInfo() != null)
                throw new IllegalStateException("A selected service instance is required");
            return URI.create(target.getScheme() + Symbol.COLON + Symbol.FORWARDSLASH + target.getRawAuthority());
        }
        if (asset.getInstances().size() != Normal._1)
            throw new IllegalStateException("A unique selected service instance is required");
        Instance instance = asset.getInstances().getFirst();
        if (!eligible(asset, instance, DateKit.current()))
            throw new IllegalStateException("Selected service instance is no longer eligible");
        URI address = address(instance);
        String path = address.getRawPath().replaceAll(Symbol.SLASH + Symbol.DOLLAR, Normal.EMPTY);
        return URI.create(origin(address) + path);
    }

    /**
     * Builds the complete request endpoint using the selected Instance or an explicit absolute protocol URL.
     *
     * @param asset request-local route
     * @return complete target URI without any definition-owned host or port fallback
     */
    public static URI endpoint(Assets asset) {
        URI operation = URI.create(Objects.requireNonNull(asset.getUrl(), "Operation path required"));
        if (operation.isAbsolute()) {
            if (asset.getInstances() != null || operation.getRawAuthority() == null || operation.getUserInfo() != null)
                throw new IllegalArgumentException("A registered API operation must be application-relative");
            return operation;
        }
        if (!asset.getUrl().startsWith(Symbol.SLASH) || operation.getRawAuthority() != null)
            throw new IllegalArgumentException("An application-relative operation path is required");
        return URI.create(resolve(asset).toASCIIString() + asset.getUrl());
    }

    /**
     * Validates an application-relative operation path before a router renders any named path placeholders. Concrete
     * endpoint construction still requires a fully rendered URI.
     *
     * @param path stored operation path or path template
     * @throws IllegalArgumentException when the operation is malformed or supplies an upstream authority
     */
    public static void validateOperation(String path) {
        if (path == null || !path.startsWith(Symbol.SLASH) || path.startsWith(Symbol.FORWARDSLASH))
            throw new IllegalArgumentException("API operation must be an application-relative path");
        int suffix = path.length();
        int query = path.indexOf(Symbol.C_QUESTION_MARK);
        int fragment = path.indexOf(Symbol.C_HASH);
        if (query >= 0)
            suffix = query;
        if (fragment >= 0)
            suffix = Math.min(suffix, fragment);
        String rendered = PATH_VARIABLE.matcher(path.substring(0, suffix)).replaceAll("value");
        URI operation = URI.create(rendered + path.substring(suffix));
        if (operation.isAbsolute() || operation.getRawAuthority() != null)
            throw new IllegalArgumentException("API operation must be an application-relative path");
    }

    /**
     * Resolves the service base URL from the instance's announced scheme, host, port and deployment context.
     *
     * @param instance announced runtime instance
     * @return validated HTTP or HTTPS listener URL
     * @throws IllegalArgumentException if the announcement lacks a usable address
     */
    public static URI address(Instance instance) {
        if (instance == null || instance.getHost() == null)
            throw new IllegalArgumentException("A complete service instance address is required");
        String scheme = instance.getScheme();
        String host = instance.getHost();
        if ((!Protocol.HTTP.name.equals(scheme) && !Protocol.HTTPS.name.equals(scheme))
                || host.isBlank() || host.contains(Symbol.SLASH) || host.contains(Symbol.AT)
                || host.contains(Symbol.QUESTION_MARK) || host.contains(Symbol.HASH))
            throw new IllegalArgumentException("Invalid service instance host or scheme");
        int port = instance.getPort() == null ? defaultPort(scheme) : instance.getPort();
        if (port < Normal._1 || port > Normal._65535)
            throw new IllegalArgumentException("Invalid service instance port");
        String path = instance.getPath() == null ? Normal.EMPTY : instance.getPath();
        URI context = URI.create(path);
        if ((!path.isEmpty() && !path.startsWith(Symbol.SLASH)) || path.startsWith(Symbol.FORWARDSLASH)
                || context.getRawQuery() != null || context.getRawFragment() != null
                || context.getPath().contains(Symbol.DOUBLE_DOT))
            throw new IllegalArgumentException("Invalid service context path");
        String authority = host.contains(Symbol.COLON) && !host.startsWith(Symbol.BRACKET_LEFT)
                ? Symbol.BRACKET_LEFT + host + Symbol.BRACKET_RIGHT
                : host;
        URI address = URI.create(scheme + Symbol.COLON + Symbol.FORWARDSLASH + authority
                + Symbol.COLON + port + path);
        if (address.getHost() == null || address.getRawUserInfo() != null || address.getPort() != port)
            throw new IllegalArgumentException("Invalid service instance host");
        return address;
    }

    /**
     * Resolves a complete registration URL into the existing instance coordinates. Explicit fields may be repeated
     * when they agree with the URL.
     *
     * @param url      absolute HTTP or HTTPS URL supplied by the service
     * @param instance announcement carrying the service metadata and optional coordinates
     * @throws IllegalArgumentException when the URL or repeated coordinates are invalid
     */
    public static void coordinates(String url, Instance instance) {
        URI announced = URI.create(url);
        String scheme = StringKit.toLowerCase(announced.getScheme());
        String host = announced.getHost();
        String path = announced.getRawPath();
        if ((!Protocol.HTTP.name.equals(scheme) && !Protocol.HTTPS.name.equals(scheme))
                || host == null || announced.getRawUserInfo() != null || announced.getRawQuery() != null
                || announced.getRawFragment() != null || announced.getRawAuthority().endsWith(Symbol.COLON))
            throw new IllegalArgumentException("An absolute HTTP or HTTPS registration URL is required");
        int port = announced.getPort() == Normal.__1 ? defaultPort(scheme) : announced.getPort();
        if (instance.getScheme() != null && !scheme.equals(instance.getScheme())
                || instance.getHost() != null && !host.equalsIgnoreCase(instance.getHost())
                || instance.getPort() != null && instance.getPort() != port
                || instance.getPath() != null && !path.equals(instance.getPath()))
            throw new IllegalArgumentException("Registration coordinates do not match the supplied URL");
        instance.setScheme(scheme);
        instance.setHost(host);
        instance.setPort(port);
        instance.setPath(path);
        address(instance);
    }

    /**
     * Returns the standard port for an already validated HTTP or HTTPS scheme.
     *
     * @param scheme registered URL scheme
     * @return HTTP port 80 or HTTPS port 443
     */
    private static int defaultPort(String scheme) {
        return Protocol.HTTPS.name.equals(scheme) ? Port._443.getPort() : Port._80.getPort();
    }

    /**
     * Returns the connection origin of a validated service address.
     *
     * @param address validated HTTP or HTTPS address
     * @return scheme and authority without the deployment context
     */
    public static String origin(URI address) {
        return address.getScheme() + Symbol.COLON + Symbol.FORWARDSLASH + address.getRawAuthority();
    }

    /**
     * Validates the independently reported listener address and weight.
     *
     * @param instance proposed service announcement
     */
    public static void validate(Instance instance) {
        address(instance);
        if (instance.getWeight() == null || instance.getWeight() < Normal._0
                || instance.getWeight() > Builder.MAX_INSTANCE_WEIGHT)
            throw new IllegalArgumentException("Invalid service instance weight");
    }

}
