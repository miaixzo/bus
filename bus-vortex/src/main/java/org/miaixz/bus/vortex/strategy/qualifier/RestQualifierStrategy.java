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
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Qualifies REST/API style requests by resolving route assets and applying route-level authorization.
 * <p>
 * The REST qualifier owns asset lookup, HTTP verb matching, route signature validation, and policy authorization.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class RestQualifierStrategy extends QualifierStrategy {

    /**
     * Creates a REST qualifier strategy.
     *
     * @param provider credential validation provider
     * @param registry asset registry
     */
    public RestQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        super(provider, registry);
    }

    /**
     * Resolves the request route and stores the matched asset in the request context.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return qualification completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            Keying.RegistrySpec requestRoute = route(exchange, context);
            String namespace = requestRoute.namespacePart();
            String type = requestRoute.typeKeyPart();
            String appId = requestRoute.appIdPart();
            String method = requestRoute.methodPart();
            String version = requestRoute.versionPart();
            Integer verb = requestRoute.verbPart();

            return Mono.defer(() -> Mono.justOrEmpty(this.registry.get(requestRoute))).switchIfEmpty(Mono.defer(() -> {
                Logger.warn(
                        false,
                        "Vortex",
                        "REST asset not found: strategy=rest-qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}, verb={}",
                        context.getX_request_ip(),
                        namespace,
                        type,
                        appId,
                        method,
                        version,
                        verb);
                return Mono.error(new ValidateException(ErrorCode._100103));
            })).flatMap(match -> {
                Assets assets = match.assets();
                context.setAssets(assets);
                Logger.info(
                        true,
                        "Vortex",
                        "REST asset resolved: strategy=rest-qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}, verb={}, policy={}, sign={}, mode={}, host={}, port={}, path={}, url={}",
                        context.getX_request_ip(),
                        assets.getNamespace_id(),
                        assets.getType(),
                        assets.getApp_id(),
                        assets.getMethod(),
                        assets.getVersion(),
                        assets.getVerb(),
                        assets.getPolicy(),
                        assets.getSign(),
                        assets.getProtocol(),
                        assets.getHost(),
                        assets.getPort(),
                        assets.getPath(),
                        assets.getUrl());
                Mono<Void> validationMono = this.method(context, assets).then(validateSignature(context, assets));
                Mono<Void> authMono = !Consts.ZERO.equals(assets.getPolicy()) ? this.authorize(context) : Mono.empty();
                return validationMono.then(authMono).then(finalizeParameters(exchange, context));
            }).then(
                    Mono.fromRunnable(
                            () -> Logger.info(
                                    true,
                                    "Vortex",
                                    "REST qualifier completed: strategy=rest-qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}",
                                    context.getX_request_ip(),
                                    namespace,
                                    type,
                                    appId,
                                    method,
                                    version)))
                    .then(chain.apply(exchange));
        });
    }

    /**
     * Validates the REST/API signature when the resolved asset enables signing.
     *
     * @param context request context
     * @param assets  resolved route asset
     * @return validation completion signal
     */
    protected Mono<Void> validateSignature(Context context, Assets assets) {
        if (assets == null || !Consts.ONE.equals(assets.getSign())) {
            return Mono.empty();
        }
        if (StringKit.isBlank(value(context, Args.SIGN))) {
            return Mono.error(new ValidateException(ErrorCode._100108));
        }
        return Mono.fromCallable(() -> {
            Map<String, Object> params = context.getParameters();
            String key = value(context, Args.METHOD);
            if (!validateSign(key + value(context, Args.TIMESTAMP), context.getHttpMethod().value(), params)) {
                throw new SignatureException(ErrorCode._100109);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Recalculates and compares the REST/API request signature.
     *
     * @param key        secret key material
     * @param httpMethod current HTTP method name
     * @param params     request parameters
     * @return {@code true} when the signature matches
     */
    protected boolean validateSign(String key, String httpMethod, Map<String, Object> params) {
        if (params == null) {
            return false;
        }
        String clientSign = value(params, Args.SIGN);
        if (clientSign == null) {
            return false;
        }

        Logger.debug(
                true,
                "Vortex",
                "Signature validation started: strategy=rest-qualifier, httpMethod={}, parameterCount={}, clientSignatureLength={}",
                httpMethod,
                params.size(),
                clientSign.length());

        Map<String, Object> paramsForSign = copyWithoutIgnoreCase(params, Args.SIGN);

        String serverSign = this.generateSignature(key, httpMethod, paramsForSign);
        boolean matched = Objects.equals(clientSign, serverSign);
        Logger.info(
                false,
                "Vortex",
                "Signature validation completed: strategy=rest-qualifier, httpMethod={}, signedParameterCount={}, matched={}, serverSignatureLength={}",
                httpMethod,
                paramsForSign.size(),
                matched,
                serverSign.length());

        return matched;
    }

    /**
     * Generates a REST/API signature using HMAC-SHA256 and Base64.
     *
     * @param key        secret key
     * @param httpMethod current HTTP method name
     * @param params     request parameters to sign
     * @return Base64-encoded signature
     */
    protected String generateSignature(String key, String httpMethod, Map<String, Object> params) {
        if (StringKit.isEmpty(key) || httpMethod == null || params == null) {
            throw new IllegalArgumentException("Key, http method, and params cannot be null or empty.");
        }

        String sortedAndEncodedParams = params.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), normalizeParameterValue(entry.getValue())))
                .filter(entry -> StringKit.isNotEmpty(entry.getValue())).sorted(Map.Entry.comparingByKey())
                .map(
                        entry -> UrlEncoder.encodeAll(entry.getKey(), Charset.UTF_8)
                                + UrlEncoder.encodeAll(entry.getValue(), Charset.UTF_8))
                .collect(Collectors.joining());

        String stringToSign = httpMethod + Symbol.LF + sortedAndEncodedParams;

        HMac hmac = Builder.hmac(Algorithm.HMACSHA256, key.getBytes(Charset.UTF_8));
        byte[] signBytes = hmac.digest(stringToSign.getBytes(Charset.UTF_8));
        return Base64.encode(signBytes);
    }

    /**
     * Converts one request parameter value to the stable string form used by REST signature generation.
     *
     * @param value source parameter value
     * @return canonical parameter value
     */
    private String normalizeParameterValue(Object value) {
        String text = value instanceof Map || value instanceof Collection
                || (value != null && value.getClass().isArray()) ? JsonKit.toJsonString(value) : String.valueOf(value);

        return UnicodeKit.toString(text);
    }

}
