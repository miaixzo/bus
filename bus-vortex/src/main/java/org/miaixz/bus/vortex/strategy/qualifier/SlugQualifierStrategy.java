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
package org.miaixz.bus.vortex.strategy.qualifier;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UnicodeKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.routing.slug.SlugRouteMatcher;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Qualifies public slug forwarding requests.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class SlugQualifierStrategy extends QualifierStrategy {

    /**
     * Matcher used to resolve public slug assets.
     */
    private final SlugRouteMatcher matcher;

    /**
     * Creates a slug qualifier strategy.
     *
     * @param matcher  slug route matcher
     * @param provider credential validation provider
     */
    public SlugQualifierStrategy(SlugRouteMatcher matcher, AuthorizeProvider provider) {
        super(provider, null);
        this.matcher = matcher;
    }

    /**
     * Returns this strategy's dynamic protocol.
     *
     * @return slug protocol number
     */
    @Override
    public Integer protocol() {
        return Args.PROTOCOL_SLUG;
    }

    /**
     * Resolves and qualifies a public slug asset.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            SlugRouteMatcher.Match match = this.matcher.match(exchange);
            if (match == null) {
                return Mono.error(new ValidateException(ErrorCode._100800));
            }
            Assets assets = match.assets();
            context.setAssets(assets);
            Logger.info(
                    true,
                    "Vortex",
                    "Public slug asset resolved: clientIp={}, method={}, version={}, verb={}, policy={}, sign={}, host={}, port={}, path={}, url={}",
                    context.getX_request_ip(),
                    assets.getMethod(),
                    assets.getVersion(),
                    assets.getVerb(),
                    assets.getPolicy(),
                    assets.getSign(),
                    assets.getHost(),
                    assets.getPort(),
                    assets.getPath(),
                    assets.getUrl());
            return method(context, assets).then(signature(context, assets)).then(authorize(context))
                    .then(finalizeParameters(exchange, context)).then(chain.apply(exchange));
        });
    }

    /**
     * Validates the optional public slug request signature.
     *
     * @param context request context
     * @param assets  resolved asset
     * @return completion signal
     */
    private Mono<Void> signature(Context context, Assets assets) {
        if (assets == null || !Consts.ONE.equals(assets.getSign())) {
            return Mono.empty();
        }
        String clientSign = value(context, Args.SIGN);
        if (StringKit.isBlank(clientSign)) {
            return Mono.error(new ValidateException(ErrorCode._100108));
        }
        return Mono.fromCallable(() -> {
            String key = assets.getMethod() + value(context, Args.TIMESTAMP);
            Map<String, Object> paramsForSign = new TreeMap<>(
                    copyWithoutIgnoreCase(context.getParameters(), Args.SIGN));
            String sortedAndEncodedParams = paramsForSign.entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), normalize(entry.getValue())))
                    .filter(entry -> StringKit.isNotEmpty(entry.getValue())).sorted(Map.Entry.comparingByKey())
                    .map(
                            entry -> UrlEncoder.encodeAll(entry.getKey(), Charset.UTF_8)
                                    + UrlEncoder.encodeAll(entry.getValue(), Charset.UTF_8))
                    .collect(Collectors.joining());
            String stringToSign = context.getHttpMethod().value() + Symbol.LF + sortedAndEncodedParams;
            HMac hmac = Builder.hmac(Algorithm.HMACSHA256, key.getBytes(Charset.UTF_8));
            String serverSign = Base64.encode(hmac.digest(stringToSign.getBytes(Charset.UTF_8)));
            if (!Objects.equals(clientSign, serverSign)) {
                throw new SignatureException(ErrorCode._100109);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Normalizes one signature input value into a stable string representation.
     *
     * @param value raw parameter value
     * @return normalized signature value
     */
    private String normalize(Object value) {
        String text = value instanceof Map || value instanceof Collection
                || (value != null && value.getClass().isArray()) ? JsonKit.toJsonString(value) : String.valueOf(value);
        return UnicodeKit.toString(text);
    }

}
