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
package org.miaixz.bus.vortex.routing.slug;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.registry.AssetsRegistry;

/**
 * Matches public slug requests to one registry asset and target template.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SlugRouteMatcher {

    /**
     * Runtime registry used to resolve slug forwarding assets.
     */
    private final AssetsRegistry registry;

    /**
     * Registry method that identifies public slug forwarding assets.
     */
    private final String slugMethod;

    /**
     * Creates a public slug route matcher.
     *
     * @param registry   runtime asset registry
     * @param slugMethod registry method for slug forwarding assets
     */
    public SlugRouteMatcher(AssetsRegistry registry, String slugMethod) {
        this.registry = registry;
        this.slugMethod = slugMethod == null ? null : slugMethod.trim();
    }

    /**
     * Matches a WebFlux exchange to one public slug route.
     *
     * @param exchange current server exchange
     * @return route match, or {@code null} when no slug route matches
     */
    public Match match(ServerWebExchange exchange) {
        if (exchange == null) {
            return null;
        }
        String namespace = exchange.getRequest().getQueryParams().getFirst(Args.NAMESPACE);
        if (StringKit.isBlank(namespace)) {
            namespace = exchange.getRequest().getHeaders().getFirst(Args.NAMESPACE);
        }
        String appId = exchange.getRequest().getQueryParams().getFirst(Args.APP_ID);
        if (StringKit.isBlank(appId)) {
            appId = exchange.getRequest().getHeaders().getFirst(Args.APP_ID);
        }
        return match(
                exchange.getRequest().getPath().value(),
                spec(namespace, appId, exchange.getRequest().getMethod().name()));
    }

    /**
     * Matches a functional server request to one public slug route.
     *
     * @param request current server request
     * @return route match, or {@code null} when no slug route matches
     */
    public Match match(ServerRequest request) {
        if (request == null) {
            return null;
        }
        String namespace = request.queryParam(Args.NAMESPACE).orElse(null);
        if (StringKit.isBlank(namespace)) {
            namespace = request.headers().firstHeader(Args.NAMESPACE);
        }
        String appId = request.queryParam(Args.APP_ID).orElse(null);
        if (StringKit.isBlank(appId)) {
            appId = request.headers().firstHeader(Args.APP_ID);
        }
        return match(request.path(), spec(namespace, appId, request.method().name()));
    }

    /**
     * Renders one slug target template by replacing its single placeholder with the matched slug.
     *
     * @param template target template
     * @param slug     matched slug value
     * @return rendered target string
     */
    public static String render(String template, String slug) {
        String placeholder = placeholder(template);
        if (placeholder == null) {
            return template;
        }
        return template.replace(placeholder, StringKit.toStringOrEmpty(slug));
    }

    /**
     * Resolves a registry asset and matches it against the request path.
     *
     * @param path request path
     * @param spec registry lookup key
     * @return route match, or {@code null} when no asset matches
     */
    private Match match(String path, Keying.RegistrySpec spec) {
        AssetsRegistry.RouteMatch routeMatch = this.registry.get(spec);
        return routeMatch == null ? null : match(path, routeMatch.assets());
    }

    /**
     * Builds the registry lookup key for one slug request.
     *
     * @param namespace namespace id from query or header
     * @param appId     application id from query or header
     * @param method    HTTP method name
     * @return registry lookup key
     */
    private Keying.RegistrySpec spec(String namespace, String appId, String method) {
        Integer verb = null;
        try {
            verb = HTTP.Method.of(method).verb();
        } catch (IllegalArgumentException e) {
            Logger.warn(false, "Vortex", e, "HTTP method mapping failed: method={}", method);
        }
        return Keying.RegistrySpec.route(namespace, Type.API, appId, this.slugMethod, Args.DEFAULT_VERSION, verb);
    }

    /**
     * Matches a request path against all route definitions from one asset.
     *
     * @param path   request path
     * @param assets resolved slug asset
     * @return best route match, or {@code null} when no route matches
     */
    private Match match(String path, Assets assets) {
        if (assets == null || !Objects.equals(Type.API.key(), assets.getType())
                || !Objects.equals(Args.PROTOCOL_SLUG, assets.getProtocol())) {
            return null;
        }
        Match best = null;
        boolean ambiguous = false;
        for (Route route : routes(assets)) {
            Match match = match(path, assets, route);
            if (match == null) {
                continue;
            }
            if (best == null || match.prefix().length() > best.prefix().length()) {
                best = match;
                ambiguous = false;
            } else if (match.prefix().length() == best.prefix().length()) {
                ambiguous = true;
            }
        }
        if (ambiguous) {
            Logger.warn(false, "Vortex", "Public slug route is ambiguous: path={}", path);
            return null;
        }
        return best;
    }

    /**
     * Matches one request path against one normalized slug route definition.
     *
     * @param path   request path
     * @param assets resolved slug asset
     * @param route  normalized route definition
     * @return route match, or {@code null} when the path does not match
     */
    private Match match(String path, Assets assets, Route route) {
        if (StringKit.isBlank(path) || route == null) {
            return null;
        }
        String prefix = normalizePrefix(route.prefix());
        String boundary = prefix + Symbol.SLASH;
        if (!path.startsWith(boundary)) {
            return null;
        }
        String tail = path.substring(boundary.length());
        int slash = tail.indexOf(Symbol.SLASH);
        String slug = slash < 0 ? tail : tail.substring(0, slash);
        if (StringKit.isBlank(slug) || slug.contains(Symbol.SLASH) || !slug.matches("[A-Za-z0-9_-]+")) {
            return null;
        }
        return new Match(assets, prefix, slug, route.template());
    }

    /**
     * Resolves all slug route definitions from one asset metadata object.
     *
     * @param assets resolved slug asset
     * @return normalized route definitions
     */
    private List<Route> routes(Assets assets) {
        Map<?, ?> metadata = metadata(assets);
        if (metadata.isEmpty()) {
            return List.of();
        }
        List<Route> resolved = new ArrayList<>();
        Object routeDefinitions = metadata.get("routes");
        if (routeDefinitions instanceof Iterable<?> values) {
            for (Object value : values) {
                Route route = route(value, assets.getUrl());
                if (route != null) {
                    resolved.add(route);
                }
            }
        } else {
            Route route = route(routeDefinitions, assets.getUrl());
            if (route != null) {
                resolved.add(route);
            }
        }
        for (String prefix : prefixes(metadata.get("prefix"))) {
            resolved.add(new Route(prefix, assets.getUrl()));
        }
        for (String prefix : prefixes(metadata.get("prefixes"))) {
            resolved.add(new Route(prefix, assets.getUrl()));
        }
        return resolved.stream().filter(route -> StringKit.isNotBlank(route.prefix()))
                .filter(route -> placeholder(route.template()) != null).toList();
    }

    /**
     * Parses and normalizes slug metadata from an asset.
     *
     * @param assets resolved slug asset
     * @return normalized metadata map
     */
    private Map<?, ?> metadata(Assets assets) {
        if (assets == null || StringKit.isBlank(assets.getMetadata())) {
            return Map.of();
        }
        try {
            return JsonKit.toMap(assets.getMetadata());
        } catch (RuntimeException e) {
            Logger.warn(false, "Vortex", e, "Public slug metadata cannot be parsed");
            return Map.of();
        }
    }

    /**
     * Resolves one route definition from a metadata map.
     *
     * @param value           raw route definition
     * @param defaultTemplate asset URL used when the route does not declare a template
     * @return normalized route definition, or {@code null} when invalid
     */
    private Route route(Object value, String defaultTemplate) {
        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }
        String prefix = normalizePrefix(StringKit.toString(map.get("prefix")));
        if (StringKit.isBlank(prefix)) {
            return null;
        }
        String template = StringKit.toString(map.get("template"));
        return new Route(prefix, StringKit.isBlank(template) ? defaultTemplate : template);
    }

    /**
     * Resolves route prefixes from either an iterable value or a comma-separated string.
     *
     * @param value raw prefix metadata value
     * @return normalized prefix list
     */
    private List<String> prefixes(Object value) {
        if (value instanceof Iterable<?> values) {
            List<String> prefixes = new ArrayList<>();
            for (Object item : values) {
                String prefix = normalizePrefix(StringKit.toString(item));
                if (StringKit.isNotBlank(prefix)) {
                    prefixes.add(prefix);
                }
            }
            return prefixes;
        }
        String text = StringKit.toString(value);
        if (StringKit.isBlank(text)) {
            return List.of();
        }
        List<String> prefixes = new ArrayList<>();
        for (String item : text.split(Symbol.COMMA)) {
            String prefix = normalizePrefix(item);
            if (StringKit.isNotBlank(prefix)) {
                prefixes.add(prefix);
            }
        }
        return prefixes;
    }

    /**
     * Normalizes a public slug prefix to start with one slash and contain no trailing slash.
     *
     * @param prefix raw public prefix
     * @return normalized prefix, or {@code null} when blank
     */
    private String normalizePrefix(String prefix) {
        if (StringKit.isBlank(prefix)) {
            return null;
        }
        String normalized = prefix.trim();
        if (!normalized.startsWith(Symbol.SLASH)) {
            normalized = Symbol.SLASH + normalized;
        }
        while (normalized.length() > 1 && normalized.endsWith(Symbol.SLASH)) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * Finds the single placeholder token in a slug target template.
     *
     * @param template target template
     * @return placeholder token including braces, or {@code null} when the template is invalid
     */
    private static String placeholder(String template) {
        if (StringKit.isBlank(template)) {
            return null;
        }
        int left = template.indexOf(Symbol.C_BRACE_LEFT);
        int right = template.indexOf(Symbol.C_BRACE_RIGHT, left + 1);
        if (left < 0 || right < 0 || right == left + 1) {
            return null;
        }
        if (template.indexOf(Symbol.C_BRACE_LEFT, right + 1) >= 0
                || template.indexOf(Symbol.C_BRACE_RIGHT, right + 1) >= 0) {
            return null;
        }
        return template.substring(left, right + 1);
    }

    /**
     * Describes one matched public slug route.
     *
     * @param assets   resolved slug asset
     * @param prefix   matched public prefix
     * @param slug     matched slug value
     * @param template target template from the asset
     */
    public record Match(Assets assets, String prefix, String slug, String template) {

    }

    /**
     * Describes one normalized slug route definition from asset metadata.
     *
     * @param prefix   public prefix to match
     * @param template target template to render
     */
    private record Route(String prefix, String template) {

    }

}
