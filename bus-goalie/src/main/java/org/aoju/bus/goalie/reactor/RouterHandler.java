package org.aoju.bus.goalie.reactor;

import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.base.entity.Message;
import org.aoju.bus.core.collection.ConcurrentHashSet;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.BusinessException;
import org.aoju.bus.core.toolkit.CollKit;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * router handler
 *
 * @author Justubborn
 * @since 2020/10/27
 */
public class RouterHandler {

    private final static String METHOD = "method";
    private final static String VERSION = "v";

    private final Set<Asset> assets = new ConcurrentHashSet<>();

    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();


    public RouterHandler(List<AssetRegistry> assetRegistries) {
        if (CollKit.isNotEmpty(assetRegistries)) {
            assetRegistries.forEach(assetRegistry -> {
                assets.addAll(assetRegistry.init());
            });
        }
    }


    @NonNull
    public Mono<ServerResponse> handle(ServerRequest request) {
        if (Objects.equals(request.method(), HttpMethod.GET)) {
            MultiValueMap<String, String> params = request.queryParams();
            return proxy(request, params);
        } else {
            return request.exchange().getFormData().flatMap(params -> proxy(request, params));
        }
    }

    Mono<ServerResponse> proxy(ServerRequest request, MultiValueMap<String, String> params) {

        String method = params.get(METHOD).parallelStream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.EM_100108));

        String version = params.get(VERSION).parallelStream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.EM_100107));

        Consumer<HttpHeaders> httpHeadersConsumer = headers -> request.headers();
        List<Asset> assetsList = assets.parallelStream()
                .filter(asset -> method.equals(asset.getMethod())).collect(Collectors.toList());
        if (assetsList.size() < 1) {
            return Mono.error(new BusinessException(ErrorCode.EM_100103));
        }
        Asset assets = assetsList.parallelStream()
                .filter(c -> version.equals(c.getVersion())).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.EM_100102));

        String baseUrl = assets.getHost() + Symbol.C_COLON + assets.getPort();
        WebClient webClient = clients.computeIfAbsent(baseUrl, client -> WebClient.create(baseUrl));
        return webClient
                .method(HttpMethod.POST)
                .uri(assets.getUrl())
                .headers(httpHeadersConsumer)
                .bodyValue(params)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return ServerResponse.ok().body(clientResponse.bodyToMono(Message.class), Message.class);
                    }
                    return Mono.error(new BusinessException(ErrorCode.EM_100509));
                });

    }
}
