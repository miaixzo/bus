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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Egress;
import org.miaixz.bus.vortex.Executor;
import org.miaixz.bus.vortex.magic.ErrorCode;

import reactor.core.publisher.Mono;

/**
 * Executes public slug forwarding requests.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SlugExecutor implements Executor<ServerRequest, ServerResponse> {

    /**
     * Matcher used to resolve target templates.
     */
    private final SlugRouteMatcher matcher;

    /**
     * Creates a slug executor.
     *
     * @param matcher slug route matcher
     */
    public SlugExecutor(SlugRouteMatcher matcher) {
        this.matcher = matcher;
    }

    /**
     * Builds the target base URL from the current asset.
     *
     * @param context request context
     * @return base URL
     */
    @Override
    public Mono<String> build(Context context) {
        return Mono.fromCallable(() -> base(context.getAssets()));
    }

    /**
     * Executes the public slug forwarding request.
     *
     * @param context request context
     * @param request server request
     * @return downstream response
     */
    @Override
    public Mono<ServerResponse> execute(Context context, ServerRequest request) {
        URI target = target(context, request);
        WebClient.RequestBodySpec bodySpec = Egress
                .request(HttpMethod.valueOf(context.getHttpMethod().value()), target);
        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            context.getHeaders().forEach((name, value) -> {
                if (StringKit.isNotBlank(name) && value != null) {
                    headers.remove(name);
                    headers.add(name, value);
                }
            });
            headers.remove(HttpHeaders.HOST);
            headers.remove(HttpHeaders.TRANSFER_ENCODING);
            headers.remove(HttpHeaders.CONTENT_LENGTH);
            if (StringKit.isNotBlank(context.getX_request_domain())) {
                headers.set(HttpHeaders.HOST, context.getX_request_domain());
                headers.set("X-Forwarded-Host", context.getX_request_domain());
            }
        });

        if (context.getHttpMethod() != Http.Method.GET && context.getHttpMethod() != Http.Method.HEAD) {
            bodySpec.body(BodyInserters.fromDataBuffers(request.exchange().getRequest().getBody()));
        }

        Logger.info(
                true,
                "Vortex",
                "Public slug forwarding request: clientIp={}, method={}, path={}, target={}",
                context.getX_request_ip(),
                context.getHttpMethod(),
                request.path(),
                target);

        return bodySpec
                .exchangeToMono(clientResponse -> clientResponse.toEntity(byte[].class).flatMap(responseEntity -> {
                    ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(responseEntity.getStatusCode());
                    responseBuilder.headers(headers -> {
                        headers.addAll(responseEntity.getHeaders());
                        headers.remove(HttpHeaders.HOST);
                        headers.remove(HttpHeaders.TRANSFER_ENCODING);
                        headers.remove(HttpHeaders.CONTENT_LENGTH);
                    });
                    byte[] body = responseEntity.getBody();
                    if (body == null || body.length == 0 || context.getHttpMethod() == Http.Method.HEAD) {
                        return responseBuilder.build();
                    }
                    return responseBuilder.bodyValue(body);
                }));
    }

    /**
     * Builds the full target URI.
     *
     * @param context request context
     * @param request server request
     * @return target URI
     */
    private URI target(Context context, ServerRequest request) {
        SlugRouteMatcher.Match match = this.matcher.match(request);
        if (match == null) {
            throw new ValidateException(ErrorCode._100800);
        }
        String rendered = SlugRouteMatcher.render(match.template(), match.slug());
        URI renderedUri = UrlKit.toURI(rendered);
        String path = appendRemaining(stripQueryAndFragment(rendered), remaining(request.path(), match));
        String target = renderedUri.isAbsolute() ? path : joinPath(base(match.assets()), path);

        List<String> queries = new ArrayList<>();
        if (StringKit.isNotBlank(renderedUri.getRawQuery())) {
            queries.add(renderedUri.getRawQuery());
        }
        String forwardQuery = query(context, request);
        if (StringKit.isNotBlank(forwardQuery)) {
            queries.add(forwardQuery);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(target);
        if (!queries.isEmpty()) {
            builder.replaceQuery(String.join(Symbol.AND, queries));
        }
        return builder.build(true).toUri();
    }

    /**
     * Builds the asset base URL.
     *
     * @param assets resolved asset
     * @return base URL
     */
    private String base(Assets assets) {
        StringBuilder builder = new StringBuilder(StringKit.toStringOrEmpty(assets.getHost()));
        if (assets.getPort() != null && assets.getPort() > 0) {
            builder.append(Symbol.COLON).append(assets.getPort());
        }
        if (StringKit.isNotBlank(assets.getPath())) {
            if (!assets.getPath().startsWith(Symbol.SLASH)) {
                builder.append(Symbol.SLASH);
            }
            builder.append(assets.getPath());
        }
        return UrlKit.normalize(builder.toString(), false);
    }

    /**
     * Builds the forwarded query string.
     *
     * @param context request context
     * @param request server request
     * @return raw query string
     */
    private String query(Context context, ServerRequest request) {
        List<String> parts = new ArrayList<>();
        Map<String, String> query = context.getQuery();
        if (!query.isEmpty()) {
            query.forEach((key, value) -> {
                if (!Args.isForwardingControlParameter(key)) {
                    parts.add(UrlKit.toQuery(Map.of(key, StringKit.toStringOrEmpty(value)), Charset.UTF_8));
                }
            });
        }
        return String.join(Symbol.AND, parts);
    }

    /**
     * Joins two path fragments without duplicating or losing the separator.
     *
     * @param base base path or URL
     * @param path child path
     * @return joined path
     */
    private String joinPath(String base, String path) {
        if (StringKit.isBlank(path)) {
            return base;
        }
        boolean baseEnds = base.endsWith(Symbol.SLASH);
        boolean pathStarts = path.startsWith(Symbol.SLASH);
        if (baseEnds && pathStarts) {
            return base + path.substring(1);
        }
        if (!baseEnds && !pathStarts) {
            return base + Symbol.SLASH + path;
        }
        return base + path;
    }

    /**
     * Returns the path suffix after the matched public prefix and slug value.
     *
     * @param path  original request path
     * @param match resolved slug match
     * @return remaining path suffix, or an empty string
     */
    private String remaining(String path, SlugRouteMatcher.Match match) {
        if (StringKit.isBlank(path) || match == null) {
            return Normal.EMPTY;
        }
        String matched = match.prefix() + Symbol.SLASH + match.slug();
        if (path.length() <= matched.length() || !path.startsWith(matched)) {
            return Normal.EMPTY;
        }
        String remaining = path.substring(matched.length());
        return remaining.startsWith(Symbol.SLASH) ? remaining : Normal.EMPTY;
    }

    /**
     * Appends the unmatched request suffix to the rendered target path.
     *
     * @param path      rendered target path
     * @param remaining unmatched request suffix
     * @return combined target path
     */
    private String appendRemaining(String path, String remaining) {
        if (StringKit.isBlank(remaining)) {
            return path;
        }
        return joinPath(path, remaining);
    }

    /**
     * Removes query and fragment components from a rendered target string.
     *
     * @param value rendered target string
     * @return path portion without query or fragment
     */
    private String stripQueryAndFragment(String value) {
        int queryIndex = value.indexOf(Symbol.C_QUESTION_MARK);
        int fragmentIndex = value.indexOf(Symbol.C_HASH);
        int end = value.length();
        if (queryIndex >= 0) {
            end = Math.min(end, queryIndex);
        }
        if (fragmentIndex >= 0) {
            end = Math.min(end, fragmentIndex);
        }
        return value.substring(0, end);
    }

}
