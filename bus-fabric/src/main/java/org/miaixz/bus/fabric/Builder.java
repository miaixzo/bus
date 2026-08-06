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
package org.miaixz.bus.fabric;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.tls.TlsVersion;

/**
 * Central catalog of shared bus-fabric constants and typed option keys.
 * <p>
 * Names describe the value contract rather than a particular caller. Protocol prefixes are retained only for values
 * defined by that protocol, while values shared across protocols use neutral names.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Builder {

    /**
     * Creates an empty constant-catalog instance.
     */
    public Builder() {
        // No initialization required.
    }

    /**
     * AIO protocol scheme.
     */
    public static final String AIO_SCHEME = "aio";

    /**
     * Session attribute key for filters.
     */
    public static final String ATTRIBUTE_FILTER = "filter";

    /**
     * Session attribute key for guard rules.
     */
    public static final String ATTRIBUTE_GUARD = "guard";

    /**
     * Session attribute key for original headers.
     */
    public static final String ATTRIBUTE_HEADERS = "headers";

    /**
     * Session attribute key for observation observer.
     */
    public static final String ATTRIBUTE_OBSERVER = "observer";

    /**
     * Session attribute key for parsed PROXY protocol metadata.
     */
    public static final String ATTRIBUTE_PROXY_HEADER = "proxyHeader";

    /**
     * Session attribute key for socket options.
     */
    public static final String ATTRIBUTE_SOCKET_OPTIONS = "socketOptions";

    /**
     * Common 16 MiB byte count.
     */
    public static final long BYTES_16_MIB = Normal._16 * Normal.MEBI;

    /**
     * Common 64 KiB byte count.
     */
    public static final int BYTES_64_KIB = Normal._64 * Normal._1024;

    /**
     * Cacheable HTTP status codes.
     */
    public static final Set<Integer> CACHE_POLICY_CACHEABLE = Set.of(
            Http.Status.OK,
            Http.Status.NON_AUTHORITATIVE_INFORMATION,
            Http.Status.NO_CONTENT,
            Http.Status.MULTIPLE_CHOICES,
            Http.Status.MOVED_PERMANENTLY,
            Http.Status.PERMANENT_REDIRECT,
            Http.Status.NOT_FOUND,
            Http.Status.METHOD_NOT_ALLOWED,
            Http.Status.GONE,
            Http.Status.URI_TOO_LONG,
            Http.Status.NOT_IMPLEMENTED);

    /**
     * Maximum representable cookie expiration instant in epoch milliseconds.
     */
    public static final long COOKIE_MAX_DATE_MILLIS = 253402300799999L;

    /**
     * Default header used to select a demultiplexed channel.
     */
    public static final String DEMUXER_DEFAULT_CHANNEL_HEADER = "X-Fabric-Channel";

    /**
     * Directory registry key for connection services.
     */
    public static final String DIRECTORY_CONNECTION = "connection";

    /**
     * Directory registry key for policy services.
     */
    public static final String DIRECTORY_POLICY = "policy";

    /**
     * Directory registry key for proxy services.
     */
    public static final String DIRECTORY_PROXY = "proxy";

    /**
     * Directory registry key for resolver services.
     */
    public static final String DIRECTORY_RESOLVER = "resolver";

    /**
     * Disk LRU journal token marking a readable entry.
     */
    public static final String DISK_LRU_CACHE_CLEAN = "CLEAN";

    /**
     * Disk LRU journal token marking an entry under edit.
     */
    public static final String DISK_LRU_CACHE_DIRTY = "DIRTY";

    /**
     * Disk LRU primary journal file name.
     */
    public static final String DISK_LRU_CACHE_JOURNAL_FILE = "journal";

    /**
     * Disk LRU backup journal file name.
     */
    public static final String DISK_LRU_CACHE_JOURNAL_FILE_BACKUP = "journal.bkp";

    /**
     * Disk LRU temporary journal file name.
     */
    public static final String DISK_LRU_CACHE_JOURNAL_FILE_TEMP = "journal.tmp";

    /**
     * Disk LRU cache legal key pattern.
     */
    public static final Pattern DISK_LRU_CACHE_LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,120}");

    /**
     * Disk LRU journal magic header.
     */
    public static final String DISK_LRU_CACHE_MAGIC = "libcore.io.DiskLruCache";

    /**
     * Disk LRU journal token recording an entry read.
     */
    public static final String DISK_LRU_CACHE_READ = "READ";

    /**
     * Disk LRU journal token recording an entry removal.
     */
    public static final String DISK_LRU_CACHE_REMOVE = "REMOVE";

    /**
     * On-disk cache metadata format version.
     */
    public static final int DISK_STORE_VERSION = 20260706;

    /**
     * Fallback lifetime in nanoseconds for successful DNS results without TTL metadata.
     */
    public static final long DNS_RESOLVER_DEFAULT_POSITIVE_TTL_NANOS = Duration.ofSeconds(Normal._60).toNanos();

    /**
     * Lifetime in nanoseconds for an authoritative empty DNS result.
     */
    public static final long DNS_RESOLVER_NEGATIVE_TTL_NANOS = Duration.ofSeconds(Normal._1).toNanos();

    /**
     * Duration of one second.
     */
    public static final Duration DURATION_1_SECOND = Duration.ofSeconds(Normal._1);

    /**
     * Duration of sixty seconds.
     */
    public static final Duration DURATION_60_SECONDS = Duration.ofSeconds(Normal._60);

    /**
     * Fabric meter invalid event counter name.
     */
    public static final String FABRIC_METER_INVALID_EVENT = "invalidEvent";

    /**
     * Stable name of the body-size limit guard.
     */
    public static final String GUARD_BODY_LIMIT_NAME = "body-limit";

    /**
     * Guard-chain name.
     */
    public static final String GUARD_CHAIN_NAME = "chain";

    /**
     * Stable name of the frame-size limit guard.
     */
    public static final String GUARD_FRAME_LIMIT_NAME = "frame-limit";

    /**
     * Stable name of the frame-rate guard.
     */
    public static final String GUARD_FRAME_RATE_NAME = "frame-rate";

    /**
     * Delay before starting the second address in one AIO Happy Eyeballs pair.
     */
    public static final Duration HAPPY_EYEBALLS_DELAY = Duration.ofMillis(250L);

    /**
     * Canonical lowercase host key used by tags and HTTP header tables.
     */
    public static final String HOST = "host";

    /**
     * Maximum time spent draining a reusable HTTP/1 response body.
     */
    public static final Duration HTTP1_CODEC_MAX_DRAIN_DURATION = Duration.ofMillis(Normal._100);

    /**
     * HTTP/2 client connection preface.
     */
    public static final String HTTP2_CONNECTION_PREFACE = "PRI * HTTP/2.0" + Symbol.CRLF + Symbol.CRLF + "SM"
            + Symbol.CRLF + Symbol.CRLF;

    /**
     * HTTP/2 receive-window threshold that triggers a WINDOW_UPDATE frame.
     */
    public static final long HTTP2_CONNECTION_WINDOW_UPDATE_THRESHOLD = Http.Setting.DEFAULT_INITIAL_WINDOW_SIZE
            / Normal._2;

    /**
     * HTTP/2 PRIORITY exclusive-dependency bit mask.
     */
    public static final int HTTP2_PRIORITY_EXCLUSIVE_MASK = Integer.MIN_VALUE;

    /**
     * Canonical HTTP Basic authentication scheme.
     */
    public static final String HTTP_AUTH_BASIC = "Basic";

    /**
     * Lowercase HTTP Basic authentication scheme used for case-insensitive matching.
     */
    public static final String HTTP_AUTH_BASIC_LOWER = "basic";

    /**
     * HTTP cache metadata field containing the response status code.
     */
    public static final String HTTP_CACHE_CODEC_META_CODE = "Fabric-Http-Code";

    /**
     * HTTP cache metadata field containing the response media type.
     */
    public static final String HTTP_CACHE_CODEC_META_MEDIA = "Fabric-Http-Media";

    /**
     * HTTP cache metadata field containing the response reason phrase.
     */
    public static final String HTTP_CACHE_CODEC_META_MESSAGE = "Fabric-Http-Message";

    /**
     * HTTP cache metadata field containing the request method.
     */
    public static final String HTTP_CACHE_CODEC_META_METHOD = "Fabric-Http-Method";

    /**
     * HTTP cache metadata field identifying the cache-record protocol.
     */
    public static final String HTTP_CACHE_CODEC_META_PROTOCOL = "Fabric-Cache-Protocol";

    /**
     * HTTP cache metadata field containing the response receive time.
     */
    public static final String HTTP_CACHE_CODEC_META_RECEIVED_AT = "Fabric-Http-Received-At";

    /**
     * HTTP cache metadata field for a stored request-header name.
     */
    public static final String HTTP_CACHE_CODEC_META_REQUEST_HEADER_NAME = "Fabric-Http-Request-Header-Name";

    /**
     * HTTP cache metadata field for a stored request-header value.
     */
    public static final String HTTP_CACHE_CODEC_META_REQUEST_HEADER_VALUE = "Fabric-Http-Request-Header-Value";

    /**
     * HTTP cache metadata field for a stored response-header name.
     */
    public static final String HTTP_CACHE_CODEC_META_RESPONSE_HEADER_NAME = "Fabric-Http-Response-Header-Name";

    /**
     * HTTP cache metadata field for a stored response-header value.
     */
    public static final String HTTP_CACHE_CODEC_META_RESPONSE_HEADER_VALUE = "Fabric-Http-Response-Header-Value";

    /**
     * HTTP cache metadata field containing the response protocol.
     */
    public static final String HTTP_CACHE_CODEC_META_RESPONSE_PROTOCOL = "Fabric-Http-Response-Protocol";

    /**
     * HTTP cache metadata field containing the request send time.
     */
    public static final String HTTP_CACHE_CODEC_META_SENT_AT = "Fabric-Http-Sent-At";

    /**
     * HTTP cache metadata field containing the request URL.
     */
    public static final String HTTP_CACHE_CODEC_META_URL = "Fabric-Http-Url";

    /**
     * HTTP request tag.
     */
    public static final String HTTP_TAG_REQUEST = "http-request";

    /**
     * HTTP response tag.
     */
    public static final String HTTP_TAG_RESPONSE = "http-response";

    /**
     * SOAP request tag.
     */
    public static final String HTTP_TAG_SOAP_REQUEST = "soap-request";

    /**
     * SOAP response tag.
     */
    public static final String HTTP_TAG_SOAP_RESPONSE = "soap-response";

    /**
     * Default KCP packet retransmission delay.
     */
    public static final Duration KCP_NETWORK_DEFAULT_RETRANSMIT_DELAY = Duration.ofMillis(Normal._200);

    /**
     * Half of the unsigned KCP sequence-number space used for wraparound comparisons.
     */
    public static final long KCP_NETWORK_HALF_SEQUENCE_SPACE = (1L << Integer.SIZE) / Normal._2;

    /**
     * KCP V2 incomplete-message reassembly deadline.
     */
    public static final Duration KCP_NETWORK_REASSEMBLY_TIMEOUT = Duration.ofSeconds(Normal._30);

    /**
     * KCP V1 packet header bytes.
     */
    public static final int KCP_PACKET_V1_HEADER_BYTES = Byte.BYTES + Byte.BYTES + Integer.BYTES + Integer.BYTES
            + Short.BYTES + Long.BYTES;

    /**
     * Maximum KCP V1 payload within one legal UDP datagram.
     */
    public static final int KCP_PACKET_V1_MAX_PAYLOAD = Normal._65535 - Normal._28 - KCP_PACKET_V1_HEADER_BYTES;

    /**
     * KCP V2 packet header bytes.
     */
    public static final int KCP_PACKET_V2_HEADER_BYTES = KCP_PACKET_V1_HEADER_BYTES + Integer.BYTES + Short.BYTES
            + Short.BYTES;

    /**
     * Maximum KCP V2 fragment payload within one legal UDP datagram.
     */
    public static final int KCP_PACKET_V2_MAX_PAYLOAD = Normal._65535 - Normal._28 - KCP_PACKET_V2_HEADER_BYTES;

    /**
     * Maximum KCP V2 reassembly bytes retained for one remote source.
     */
    public static final long KCP_REASSEMBLY_SOURCE_MAX_BYTES = Normal._32 * Normal.MEBI;

    /**
     * Event tag key containing a lifecycle-scope name.
     */
    public static final String LIFECYCLE_SCOPE_NAME = "name";

    /**
     * Duration suffix appended to metric-family names.
     */
    public static final String METER_EVENT_OBSERVER_DURATION = ".duration";

    /**
     * Canonical failure label used by event results and failure metrics.
     */
    public static final String METER_EVENT_OBSERVER_FAILURE = "failure";

    /**
     * Multipart media-type parameter naming the boundary token.
     */
    public static final String MULTIPART_BODY_BOUNDARY_PARAMETER = "boundary";

    /**
     * Typed option for the HTTP User-Agent value.
     * <p>
     * Absence and explicit null both use the framework default User-Agent.
     */
    public static final Options.Key<String> OPTION_HTTP_USER_AGENT = Options.key("http.userAgent", String.class);

    /**
     * Typed option for the positive payload materialization limit.
     * <p>
     * Absence uses {@link Normal#MEBI_64}; explicit null is invalid.
     */
    public static final Options.Key<Long> OPTION_MATERIALIZE_MAX_BYTES = Options
            .key("materialize.maxBytes", Long.class);

    /**
     * Typed stable Destination option for maximum multiplex streams.
     * <p>
     * Absence means one; explicit null is invalid.
     */
    public static final Options.Key<Integer> OPTION_MAX_MULTIPLEX_STREAMS = Options
            .key("maxMultiplexStreams", Integer.class);

    /**
     * Typed stable Destination option indicating multiplex capability.
     * <p>
     * Absence and explicit null are derived from the Protocol.
     */
    public static final Options.Key<Boolean> OPTION_MULTIPLEX = Options.key("multiplex", Boolean.class);

    /**
     * Typed stable Destination option carrying the request protocol name.
     * <p>
     * Absence and explicit null both use the request Protocol.
     */
    public static final Options.Key<String> OPTION_PROTOCOL = Options.key("protocol", String.class);

    /**
     * Typed stable Destination option carrying a normalized proxy URI.
     * <p>
     * Absence and explicit null both mean a direct route.
     */
    public static final Options.Key<String> OPTION_ROUTE_PROXY = Options.key("route.proxy", String.class);

    /**
     * Typed stable Destination option indicating an HTTP proxy tunnel.
     * <p>
     * Absence and explicit null both mean false.
     */
    public static final Options.Key<Boolean> OPTION_ROUTE_TUNNEL = Options.key("route.tunnel", Boolean.class);

    /**
     * Typed stable Destination option indicating secure transport.
     * <p>
     * Absence and explicit null are derived from the Address.
     */
    public static final Options.Key<Boolean> OPTION_SECURE = Options.key("secure", Boolean.class);

    /**
     * Typed option for the TCP server listen backlog.
     * <p>
     * Absence uses the SocketOptions default; explicit null is invalid.
     */
    public static final Options.Key<Integer> OPTION_SOCKET_BACKLOG = Options.key("socket.backlog", Integer.class);

    /**
     * Typed option for operation-time Socket idle timeout.
     * <p>
     * Absence uses the SocketOptions default; explicit null is invalid.
     */
    public static final Options.Key<Duration> OPTION_SOCKET_IDLE_TIMEOUT = Options
            .key("socket.idleTimeout", Duration.class);

    /**
     * Typed option for Socket I/O thread count.
     * <p>
     * Absence uses the SocketOptions default; explicit null is invalid.
     */
    public static final Options.Key<Integer> OPTION_SOCKET_IO_THREADS = Options.key("socket.ioThreads", Integer.class);

    /**
     * Typed option for immutable JDK Socket channel options.
     * <p>
     * Absence and explicit null both mean an empty Map.
     */
    public static final Options.Key<Map> OPTION_SOCKET_OPTIONS = Options.key("socket.socketOptions", Map.class);

    /**
     * Typed option for per-session Socket read buffer size.
     * <p>
     * Absence uses the SocketOptions default; explicit null is invalid.
     */
    public static final Options.Key<Integer> OPTION_SOCKET_READ_BUFFER_SIZE = Options
            .key("socket.readBufferSize", Integer.class);

    /**
     * Typed option for retaining one reusable Socket read buffer.
     * <p>
     * Absence uses the SocketOptions default; explicit null is invalid.
     */
    public static final Options.Key<Boolean> OPTION_SOCKET_RETAIN_READ_BUFFER = Options
            .key("socket.retainReadBuffer", Boolean.class);

    /**
     * Typed option for retained Socket write chunk count.
     * <p>
     * Absence uses the SocketOptions default; explicit null is invalid.
     */
    public static final Options.Key<Integer> OPTION_SOCKET_WRITE_CHUNK_COUNT = Options
            .key("socket.writeChunkCount", Integer.class);

    /**
     * Typed option for maximum bytes in one low-level Socket write chunk.
     * <p>
     * Absence uses the SocketOptions default; explicit null is invalid.
     */
    public static final Options.Key<Integer> OPTION_SOCKET_WRITE_CHUNK_SIZE = Options
            .key("socket.writeChunkSize", Integer.class);

    /**
     * Typed option for the shared protocol timeout policy.
     * <p>
     * Absence and explicit null both use {@link Timeout#defaults()}.
     */
    public static final Options.Key<Timeout> OPTION_TIMEOUT = Options.key("timeout", Timeout.class);

    /**
     * Typed stable Destination option indicating TLS use.
     * <p>
     * Absence and explicit null both mean false.
     */
    public static final Options.Key<Boolean> OPTION_TLS = Options.key("tls", Boolean.class);

    /**
     * Reusable version-capture pattern suffix applied by platform browser classifiers.
     */
    public static final String PLATFORM_BROWSER_VERSION_PATTERN = "[\\/ ]([\\d\\w\\.\\-]+)";

    /**
     * PROXY protocol command indicating proxied endpoint metadata.
     */
    public static final String PROXY_HEADER_COMMAND_PROXY = "PROXY";

    /**
     * PROXY protocol TCP-over-IPv4 family token.
     */
    public static final String PROXY_HEADER_PROTOCOL_TCP4 = "TCP4";

    /**
     * PROXY protocol TCP-over-IPv6 family token.
     */
    public static final String PROXY_HEADER_PROTOCOL_TCP6 = "TCP6";

    /**
     * PROXY protocol token for an unknown address family.
     */
    public static final String PROXY_HEADER_PROTOCOL_UNKNOWN = "UNKNOWN";

    /**
     * Maximum accepted PROXY protocol v1 header-line length in bytes.
     */
    public static final int PROXY_HEADER_READER_MAX_LINE_BYTES = 108;

    /**
     * Stable proxy-plan identifier for a direct connection.
     */
    public static final String PROXY_PLAN_DIRECT_ID = "direct";

    /**
     * Stable proxy-plan identifier for an inherited policy.
     */
    public static final String PROXY_PLAN_INHERIT_ID = "inherit";

    /**
     * Stable proxy-plan identifier for system proxy selection.
     */
    public static final String PROXY_PLAN_SYSTEM_ID = "system";

    /**
     * Stable proxy-plan identifier for an HTTP proxy.
     */
    public static final String PROXY_PLAN_HTTP_ID = "http";

    /**
     * Stable proxy-plan identifier for a SOCKS proxy.
     */
    public static final String PROXY_PLAN_SOCKS_ID = "socks";

    /**
     * Classpath resource name of the encoded public-suffix list.
     */
    public static final String PUBLIC_SUFFIX_RESOURCE = "suffixes.gz";

    /**
     * Canonical route key used by the route guard and directory registry.
     */
    public static final String ROUTE = "route";

    /**
     * Maximum route-selection backoff duration.
     */
    public static final Duration SELECTOR_MAX_BACKOFF = Duration.ofMinutes(Normal._5);

    /**
     * SOAP envelope namespace URI.
     */
    public static final String SOAP_BODY_SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * SOAP envelope namespace prefix.
     */
    public static final String SOAP_BODY_SOAP_PREFIX = "soap";

    /**
     * SOAP method namespace prefix.
     */
    public static final String SOAP_METHOD_PREFIX = "m";

    /**
     * Namespace URI for generated SOAP extension headers.
     */
    public static final String SOAP_X_HEADER_NAMESPACE = "urn:bus:fabric:soap:header";

    /**
     * Namespace prefix for generated SOAP extension headers.
     */
    public static final String SOAP_X_HEADER_PREFIX = "h";

    /**
     * Socket server accept activity name.
     */
    public static final String SOCKET_ACTIVITY_ACCEPT = "socket-server-accept";

    /**
     * Socket server read activity name.
     */
    public static final String SOCKET_ACTIVITY_READ = "socket-server-read";

    /**
     * Socket open tag.
     */
    public static final String SOCKET_TAG_OPEN = "socket-open";

    /**
     * URI scheme selecting KCP socket transport.
     */
    public static final String SOCKET_X_KCP_SCHEME = "kcp";

    /**
     * SSE read activity name.
     */
    public static final String SSE_ACTIVITY_READ = "sse-read";

    /**
     * SSE retry activity name.
     */
    public static final String SSE_ACTIVITY_RETRY = "sse-retry";

    /**
     * SSE wire prefix for a data field.
     */
    public static final String SSE_BODY_DATA_PREFIX = "data" + Symbol.COLON + Symbol.SPACE;

    /**
     * SSE wire prefix for an event-type field.
     */
    public static final String SSE_BODY_EVENT_PREFIX = "event" + Symbol.COLON + Symbol.SPACE;

    /**
     * SSE wire prefix for an event-identifier field.
     */
    public static final String SSE_BODY_ID_PREFIX = "id" + Symbol.COLON + Symbol.SPACE;

    /**
     * SSE wire prefix for a retry field.
     */
    public static final String SSE_BODY_RETRY_PREFIX = "retry" + Symbol.COLON + Symbol.SPACE;

    /**
     * SSE default event name.
     */
    public static final String SSE_DEFAULT_EVENT = "message";

    /**
     * Initial SSE reconnection delay.
     */
    public static final Duration SSE_RETRY_DEFAULT_CURRENT = Duration.ofSeconds(Normal._3);

    /**
     * Maximum SSE reconnection delay.
     */
    public static final Duration SSE_RETRY_DEFAULT_MAX_DELAY = Duration.ofSeconds(Normal._30);

    /**
     * Dispatch-key prefix for SSE runner activities.
     */
    public static final String SSE_RUNNER_DISPATCH_PREFIX = "sse" + Symbol.COLON + Symbol.FORWARDSLASH;

    /**
     * HTTP header carrying the last SSE event identifier.
     */
    public static final String SSE_RUNNER_LAST_EVENT_ID = "Last-Event-ID";

    /**
     * SSE event tag.
     */
    public static final String SSE_TAG_EVENT = "sse-event";

    /**
     * SSE open tag.
     */
    public static final String SSE_TAG_OPEN = "sse-open";

    /**
     * SSE response tag.
     */
    public static final String SSE_TAG_RESPONSE = "sse-response";

    /**
     * STOMP ACK command.
     */
    public static final String STOMP_COMMAND_ACK = "ACK";

    /**
     * STOMP CONNECT command.
     */
    public static final String STOMP_COMMAND_CONNECT = "CONNECT";

    /**
     * STOMP CONNECTED command.
     */
    public static final String STOMP_COMMAND_CONNECTED = "CONNECTED";

    /**
     * STOMP DISCONNECT command.
     */
    public static final String STOMP_COMMAND_DISCONNECT = "DISCONNECT";

    /**
     * STOMP ERROR command.
     */
    public static final String STOMP_COMMAND_ERROR = "ERROR";

    /**
     * STOMP MESSAGE command.
     */
    public static final String STOMP_COMMAND_MESSAGE = "MESSAGE";

    /**
     * STOMP NACK command.
     */
    public static final String STOMP_COMMAND_NACK = "NACK";

    /**
     * STOMP RECEIPT command.
     */
    public static final String STOMP_COMMAND_RECEIPT = "RECEIPT";

    /**
     * STOMP SEND command.
     */
    public static final String STOMP_COMMAND_SEND = "SEND";

    /**
     * STOMP SUBSCRIBE command.
     */
    public static final String STOMP_COMMAND_SUBSCRIBE = "SUBSCRIBE";

    /**
     * STOMP UNSUBSCRIBE command.
     */
    public static final String STOMP_COMMAND_UNSUBSCRIBE = "UNSUBSCRIBE";

    /**
     * STOMP accept-version header.
     */
    public static final String STOMP_HEADER_ACCEPT_VERSION = "accept-version";

    /**
     * STOMP destination header.
     */
    public static final String STOMP_HEADER_DESTINATION = "destination";

    /**
     * STOMP heart-beat header.
     */
    public static final String STOMP_HEADER_HEART_BEAT = "heart-beat";

    /**
     * STOMP id header.
     */
    public static final String STOMP_HEADER_ID = "id";

    /**
     * STOMP login header.
     */
    public static final String STOMP_HEADER_LOGIN = "login";

    /**
     * STOMP message-id header.
     */
    public static final String STOMP_HEADER_MESSAGE_ID = "message-id";

    /**
     * STOMP passcode header.
     */
    public static final String STOMP_HEADER_PASSCODE = "passcode";

    /**
     * STOMP receipt header.
     */
    public static final String STOMP_HEADER_RECEIPT = "receipt";

    /**
     * STOMP receipt-id header.
     */
    public static final String STOMP_HEADER_RECEIPT_ID = "receipt-id";

    /**
     * STOMP subscription header.
     */
    public static final String STOMP_HEADER_SUBSCRIPTION = "subscription";

    /**
     * STOMP queue destination prefix.
     */
    public static final String STOMP_QUEUE_PREFIX = Symbol.SLASH + "queue";

    /**
     * STOMP connect tag.
     */
    public static final String STOMP_TAG_CONNECT = "stomp-connect";

    /**
     * STOMP connected tag.
     */
    public static final String STOMP_TAG_CONNECTED = "stomp-connected";

    /**
     * STOMP error tag.
     */
    public static final String STOMP_TAG_ERROR = "stomp-error";

    /**
     * STOMP open tag.
     */
    public static final String STOMP_TAG_OPEN = "stomp-open";

    /**
     * STOMP read tag.
     */
    public static final String STOMP_TAG_READ = "stomp-read";

    /**
     * STOMP write tag.
     */
    public static final String STOMP_TAG_WRITE = "stomp-write";

    /**
     * STOMP topic suffix matching multiple destination levels.
     */
    public static final String STOMP_TOPIC_MULTI_LEVEL_WILDCARD = Symbol.SLASH + Symbol.STAR + Symbol.STAR;

    /**
     * STOMP topic destination prefix.
     */
    public static final String STOMP_TOPIC_PREFIX = Symbol.SLASH + "topic";

    /**
     * STOMP topic suffix matching one destination level.
     */
    public static final String STOMP_TOPIC_SINGLE_LEVEL_WILDCARD = Symbol.SLASH + Symbol.STAR;

    /**
     * STOMP 1.2 protocol version.
     */
    public static final String STOMP_VERSION_1_2 = "1.2";

    /**
     * Listener action tag key.
     */
    public static final String TAG_ACTION = "action";

    /**
     * Retry attempt tag key.
     */
    public static final String TAG_ATTEMPT = "attempt";

    /**
     * Byte count tag key.
     */
    public static final String TAG_BYTES = "bytes";

    /**
     * Cache action tag key.
     */
    public static final String TAG_CACHE = "cache";

    /**
     * Status code tag key.
     */
    public static final String TAG_CODE = "code";

    /**
     * Retry delay tag key.
     */
    public static final String TAG_DELAY = "delay";

    /**
     * Exception class tag key.
     */
    public static final String TAG_EXCEPTION = "exception";

    /**
     * Cache key tag key.
     */
    public static final String TAG_KEY = "key";

    /**
     * Module tag key.
     */
    public static final String TAG_MODULE = "module";

    /**
     * Stable operation identifier tag key shared by one logical lifecycle.
     */
    public static final String TAG_OPERATION_ID = "operationId";

    /**
     * Phase tag key.
     */
    public static final String TAG_PHASE = "phase";

    /**
     * Port tag key.
     */
    public static final String TAG_PORT = "port";

    /**
     * Protocol tag key.
     */
    public static final String TAG_PROTOCOL = "protocol";

    /**
     * Redacted tag value marker.
     */
    public static final String TAG_REDACTED = "<redacted>";

    /**
     * Result tag key.
     */
    public static final String TAG_RESULT = "result";

    /**
     * Lifecycle source tag key.
     */
    public static final String TAG_SOURCE = "source";

    /**
     * URL tag key.
     */
    public static final String TAG_URL = "url";

    /**
     * Default TCP, TLS, and HTTP connection plus network read/write deadline.
     */
    public static final Duration TIMEOUT_DEFAULT_NETWORK = Duration.ofSeconds(Normal._10);

    /**
     * Default TLS protocol versions.
     */
    public static final List<String> TLS_SETTINGS_DEFAULT_VERSIONS = List.of(TlsVersion.TLSv1_3.javaName());

    /**
     * Bit mask selecting the low seven bits of an unsigned encoded value.
     */
    public static final int UNSIGNED_7_BIT_MASK = 127;

    /**
     * Unsigned byte mask.
     */
    public static final int UNSIGNED_BYTE_MASK = 0xFF;

    /**
     * Unsigned 32-bit integer mask.
     */
    public static final long UNSIGNED_INT_MASK = 0xffff_ffffL;

    /**
     * WebSocket server accept activity name.
     */
    public static final String WEBSOCKET_ACTIVITY_ACCEPT = "websocket-server-accept";

    /**
     * WebSocket close code indicating that an endpoint is going away, as defined by RFC 6455.
     */
    public static final int WEBSOCKET_CLOSE_GOING_AWAY_CODE = 1001;

    /**
     * WebSocket internal-error close code.
     */
    public static final int WEBSOCKET_CLOSE_INTERNAL_ERROR = 1011;

    /**
     * WebSocket invalid-payload close code.
     */
    public static final int WEBSOCKET_CLOSE_INVALID_PAYLOAD = 1007;

    /**
     * WebSocket message-too-large close code.
     */
    public static final int WEBSOCKET_CLOSE_MESSAGE_TOO_LARGE = 1009;

    /**
     * WebSocket protocol-error close code.
     */
    public static final int WEBSOCKET_CLOSE_PROTOCOL_ERROR = 1002;

    /**
     * Maximum payload length, in bytes, permitted in a WebSocket control frame by RFC 6455.
     */
    public static final int WEBSOCKET_CONTROL_PAYLOAD_MAX_BYTES = 125;

    /**
     * WebSocket unsigned 16-bit payload length marker.
     */
    public static final int WEBSOCKET_LENGTH_16_MARKER = 126;

    /**
     * WebSocket maximum application close code.
     */
    public static final int WEBSOCKET_MAX_APPLICATION_CLOSE_CODE = 4999;

    /**
     * WebSocket maximum protocol close code.
     */
    public static final int WEBSOCKET_MAX_PROTOCOL_CLOSE_CODE = 1014;

    /**
     * WebSocket maximum close reason bytes.
     */
    public static final int WEBSOCKET_MAX_REASON_BYTES = 123;

    /**
     * WebSocket minimum application close code.
     */
    public static final int WEBSOCKET_MIN_APPLICATION_CLOSE_CODE = 3000;

    /**
     * WebSocket binary opcode.
     */
    public static final int WEBSOCKET_OPCODE_BINARY = 0x2;

    /**
     * WebSocket opcode bit mask.
     */
    public static final int WEBSOCKET_OPCODE_MASK = 0x0F;

    /**
     * WebSocket ping opcode.
     */
    public static final int WEBSOCKET_OPCODE_PING = 0x9;

    /**
     * WebSocket pong opcode.
     */
    public static final int WEBSOCKET_OPCODE_PONG = 0xA;

    /**
     * WebSocket open tag.
     */
    public static final String WEBSOCKET_OPEN = "websocket-open";

    /**
     * WebSocket ping tag and activity name.
     */
    public static final String WEBSOCKET_PING = "websocket-ping";

    /**
     * WebSocket read tag and activity name.
     */
    public static final String WEBSOCKET_READ = "websocket-read";

    /**
     * WebSocket reserved abnormal close code.
     */
    public static final int WEBSOCKET_RESERVED_ABNORMAL_CODE = 1006;

    /**
     * WebSocket reserved no-status close code.
     */
    public static final int WEBSOCKET_RESERVED_NO_STATUS_CODE = 1005;

    /**
     * WebSocket RSV bit mask.
     */
    public static final int WEBSOCKET_RSV_MASK = 0x70;

    /**
     * Diagnostic operation name used when materializing an outbound WebSocket payload.
     */
    public static final String WEBSOCKET_SEND_MATERIALIZE_OPERATION = "WebSocketSession.send(Payload)";

    /**
     * Lowest unescaped Unicode code point accepted by WebSocket text-frame validation.
     */
    public static final char WEBSOCKET_TEXT_MIN_CODE_POINT = 0x20;

    /**
     * WebSocket write tag.
     */
    public static final String WEBSOCKET_WRITE = "websocket-write";

}
