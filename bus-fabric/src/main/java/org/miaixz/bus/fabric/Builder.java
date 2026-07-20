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
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsVersion;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.protocol.CookieJar;
import org.miaixz.bus.fabric.protocol.http.agent.Browser;
import org.miaixz.bus.fabric.protocol.http.agent.ClientOs;
import org.miaixz.bus.fabric.protocol.http.agent.Device;
import org.miaixz.bus.fabric.protocol.http.agent.Engine;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuthenticator;
import org.miaixz.bus.fabric.protocol.http.cache.HttpCache;

/**
 * Module-level constants reserved for fabric builder APIs, options, attributes, tags, and protocol defaults.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Builder {

    /**
     * Creates a root builder marker.
     */
    public Builder() {
        // No initialization required.
    }

    /**
     * Numeric value 1000.
     */
    public static final int _1000 = 1000;

    /**
     * Numeric value 1001.
     */
    public static final int _1001 = 1001;

    /**
     * Numeric value 125.
     */
    public static final int _125 = 125;

    /**
     * Numeric value 127.
     */
    public static final int _127 = 127;

    /**
     * AIO protocol scheme.
     */
    public static final String AIO_SCHEME = "aio";

    /**
     * Common 16 MiB byte count.
     */
    public static final long BYTES_16_MIB = Normal._16 * Normal.MEBI;

    /**
     * Common 64 KiB byte count.
     */
    public static final int BYTES_64_KIB = Normal._64 * Normal._1024;

    /**
     * Default maximum bytes allowed when materializing a payload.
     */
    public static final long DEFAULT_MATERIALIZE_MAX_BYTES = Normal._64 * Normal.MEBI;

    /**
     * Duration of one second.
     */
    public static final Duration DURATION_1_SECOND = Duration.ofSeconds(Normal._1);

    /**
     * Duration of sixty seconds.
     */
    public static final Duration DURATION_60_SECONDS = Duration.ofSeconds(Normal._60);

    /**
     * Host field value.
     */
    public static final String HOST = "host";

    /**
     * Route field value.
     */
    public static final String ROUTE = "route";

    /**
     * SOAP method namespace prefix.
     */
    public static final String SOAP_METHOD_PREFIX = "m";

    /**
     * Unsigned byte mask.
     */
    public static final int UNSIGNED_BYTE_MASK = 0xFF;

    /**
     * Unsigned 32-bit integer mask.
     */
    public static final long UNSIGNED_INT_MASK = 0xffff_ffffL;

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
     * Typed option for the positive payload materialization limit.
     * <p>
     * Absence uses {@link #DEFAULT_MATERIALIZE_MAX_BYTES}; explicit null is invalid.
     */
    public static final Options.Key<Long> OPTION_MATERIALIZE_MAX_BYTES = Options
            .key("materialize.maxBytes", Long.class);

    /**
     * Typed option for the shared protocol timeout policy.
     * <p>
     * Absence and explicit null both use {@link Timeout#defaults()}.
     */
    public static final Options.Key<Timeout> OPTION_TIMEOUT = Options.key("timeout", Timeout.class);

    /**
     * Typed option for the generic TLS context.
     * <p>
     * Absence leaves the generic context unconfigured; explicit null disables the generic value.
     */
    public static final Options.Key<TlsContext> OPTION_TLS_CONTEXT = Options.key("tlsContext", TlsContext.class);

    /**
     * Typed option for generic TLS settings.
     * <p>
     * Absence and explicit null both use {@link TlsSettings#defaults()}.
     */
    public static final Options.Key<TlsSettings> OPTION_TLS_SETTINGS = Options.key("tlsSettings", TlsSettings.class);

    /**
     * Typed option for the HTTP proxy plan.
     * <p>
     * Absence selects through the system proxy selector; explicit null forces a direct route without consulting it.
     */
    public static final Options.Key<ProxyPlan> OPTION_HTTP_PROXY = Options.key("http.proxy", ProxyPlan.class);

    /**
     * Typed option for the HTTP cache.
     * <p>
     * Absence and explicit null both disable caching.
     */
    public static final Options.Key<HttpCache> OPTION_HTTP_CACHE = Options.key("http.cache", HttpCache.class);

    /**
     * Typed option for the HTTP cookie jar.
     * <p>
     * Absence uses the Context-level memory jar; explicit null disables cookies.
     */
    public static final Options.Key<CookieJar> OPTION_HTTP_COOKIE_JAR = Options.key("http.cookieJar", CookieJar.class);

    /**
     * Typed option for HTTP authentication.
     * <p>
     * Absence does not initiate authentication; explicit null disables authentication.
     */
    public static final Options.Key<HttpAuthenticator> OPTION_HTTP_AUTHENTICATOR = Options
            .key("http.authenticator", HttpAuthenticator.class);

    /**
     * Typed option for the HTTP User-Agent value.
     * <p>
     * Absence and explicit null both use the framework default User-Agent.
     */
    public static final Options.Key<String> OPTION_HTTP_USER_AGENT = Options.key("http.userAgent", String.class);

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
    @SuppressWarnings("rawtypes")
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
     * Typed option for a Socket-scoped TLS context.
     * <p>
     * Absence falls back to {@link #OPTION_TLS_CONTEXT}; explicit null disables the Socket TLS context.
     */
    public static final Options.Key<TlsContext> OPTION_SOCKET_TLS_CONTEXT = Options
            .key("socket.tlsContext", TlsContext.class);

    /**
     * Typed option for Socket-scoped TLS settings.
     * <p>
     * Absence falls back to {@link #OPTION_TLS_SETTINGS}; explicit null uses default TLS settings.
     */
    public static final Options.Key<TlsSettings> OPTION_SOCKET_TLS_SETTINGS = Options
            .key("socket.tlsSettings", TlsSettings.class);

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
     * Typed stable Destination option indicating TLS use.
     * <p>
     * Absence and explicit null both mean false.
     */
    public static final Options.Key<Boolean> OPTION_TLS = Options.key("tls", Boolean.class);

    /**
     * Typed stable Destination option indicating secure transport.
     * <p>
     * Absence and explicit null are derived from the Address.
     */
    public static final Options.Key<Boolean> OPTION_SECURE = Options.key("secure", Boolean.class);

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
     * Typed stable Destination option for maximum multiplex streams.
     * <p>
     * Absence means one; explicit null is invalid.
     */
    public static final Options.Key<Integer> OPTION_MAX_MULTIPLEX_STREAMS = Options
            .key("maxMultiplexStreams", Integer.class);

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
     * Method tag key.
     */
    public static final String TAG_METHOD = "method";

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
     * Cacheable HTTP status codes.
     */
    public static final Set<Integer> CACHE_POLICY_CACHEABLE = Set.of(
            HTTP.HTTP_OK,
            HTTP.HTTP_NOT_AUTHORITATIVE,
            HTTP.HTTP_NO_CONTENT,
            HTTP.HTTP_MULT_CHOICE,
            HTTP.HTTP_MOVED_PERM,
            HTTP.HTTP_PERM_REDIRECT,
            HTTP.HTTP_NOT_FOUND,
            HTTP.HTTP_BAD_METHOD,
            HTTP.HTTP_GONE,
            HTTP.HTTP_REQ_TOO_LONG,
            HTTP.HTTP_NOT_IMPLEMENTED);

    /**
     * Disk lru cache clean value.
     */
    public static final String DISK_LRU_CACHE_CLEAN = "CLEAN";

    /**
     * Disk lru cache dirty value.
     */
    public static final String DISK_LRU_CACHE_DIRTY = "DIRTY";

    /**
     * Disk lru cache journal file value.
     */
    public static final String DISK_LRU_CACHE_JOURNAL_FILE = "journal";

    /**
     * Disk lru cache journal file backup value.
     */
    public static final String DISK_LRU_CACHE_JOURNAL_FILE_BACKUP = "journal.bkp";

    /**
     * Disk lru cache journal file temp value.
     */
    public static final String DISK_LRU_CACHE_JOURNAL_FILE_TEMP = "journal.tmp";

    /**
     * Disk LRU cache legal key pattern.
     */
    public static final Pattern DISK_LRU_CACHE_LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,120}");

    /**
     * Disk lru cache magic value.
     */
    public static final String DISK_LRU_CACHE_MAGIC = "libcore.io.DiskLruCache";

    /**
     * Disk lru cache read value.
     */
    public static final String DISK_LRU_CACHE_READ = "READ";

    /**
     * Disk lru cache remove value.
     */
    public static final String DISK_LRU_CACHE_REMOVE = "REMOVE";

    /**
     * Disk store version value.
     */
    public static final int DISK_STORE_VERSION = 20260706;

    /**
     * Guard chain name value.
     */
    public static final String GUARD_CHAIN_NAME = "chain";

    /**
     * Limit guard name value.
     */
    public static final String GUARD_FRAME_LIMIT_GUARD_NAME = "frame-limit";

    /**
     * Limit guard name value.
     */
    public static final String LIMIT_GUARD_NAME = "body-limit";

    /**
     * Rate guard name value.
     */
    public static final String RATE_GUARD_NAME = "frame-rate";

    /**
     * Tls guard name value.
     */
    public static final String TLS_GUARD_NAME = "tls";

    /**
     * DNS no-TTL marker.
     */
    public static final Duration DNS_NO_TTL = Duration.ZERO;

    /**
     * Kcp network default retransmit delay value.
     */
    public static final Duration KCP_NETWORK_DEFAULT_RETRANSMIT_DELAY = Duration.ofMillis(Normal._200);

    /**
     * Kcp network half sequence space value.
     */
    public static final long KCP_NETWORK_HALF_SEQUENCE_SPACE = (UNSIGNED_INT_MASK + Normal._1) / Normal._2;

    /**
     * Legacy KCP packet header bytes.
     */
    public static final int KCP_PACKET_LEGACY_HEADER_BYTES = Long.BYTES;

    /**
     * KCP V1 packet header bytes.
     */
    public static final int KCP_PACKET_V1_HEADER_BYTES = Byte.BYTES + Byte.BYTES + Integer.BYTES + Integer.BYTES
            + Short.BYTES + Long.BYTES;

    /**
     * KCP V2 packet header bytes.
     */
    public static final int KCP_PACKET_V2_HEADER_BYTES = KCP_PACKET_V1_HEADER_BYTES + Integer.BYTES + Short.BYTES
            + Short.BYTES;

    /**
     * Maximum KCP V1 payload within one legal UDP datagram.
     */
    public static final int KCP_PACKET_V1_MAX_PAYLOAD = Normal._65535 - Normal._28 - KCP_PACKET_V1_HEADER_BYTES;

    /**
     * Maximum KCP V2 fragment payload within one legal UDP datagram.
     */
    public static final int KCP_PACKET_V2_MAX_PAYLOAD = Normal._65535 - Normal._28 - KCP_PACKET_V2_HEADER_BYTES;

    /**
     * Compatibility alias for the V1 packet header size.
     */
    public static final int KCP_PACKET_HEADER_BYTES = KCP_PACKET_V1_HEADER_BYTES;

    /**
     * Compatibility alias for the V1 maximum packet payload.
     */
    public static final int KCP_PACKET_MAX_PAYLOAD = KCP_PACKET_V1_MAX_PAYLOAD;

    /**
     * Maximum complete KCP logical message bytes.
     */
    public static final long KCP_NETWORK_MAX_MESSAGE_BYTES = BYTES_16_MIB;

    /**
     * Maximum complete logical bytes retained by the KCP outbound queue.
     */
    public static final long KCP_NETWORK_MAX_OUTBOUND_QUEUE_BYTES = Normal._64 * Normal.MEBI;

    /**
     * Maximum active KCP V2 message reassemblies.
     */
    public static final int KCP_NETWORK_MAX_ACTIVE_REASSEMBLIES = Normal._64;

    /**
     * Maximum total bytes retained by all KCP V2 reassemblies.
     */
    public static final long KCP_NETWORK_MAX_REASSEMBLY_BYTES = Normal._64 * Normal.MEBI;

    /**
     * Maximum KCP V2 reassembly bytes retained for one source.
     */
    public static final long KCP_NETWORK_MAX_SOURCE_REASSEMBLY_BYTES = Normal._32 * Normal.MEBI;

    /**
     * KCP V2 incomplete-message reassembly deadline.
     */
    public static final Duration KCP_NETWORK_REASSEMBLY_TIMEOUT = Duration.ofSeconds(Normal._30);

    /**
     * Maximum retransmissions allowed for one KCP packet.
     */
    public static final int KCP_NETWORK_MAX_RETRANSMISSIONS = Normal._8;

    /**
     * Proxy header command proxy value.
     */
    public static final String PROXY_HEADER_COMMAND_PROXY = "PROXY";

    /**
     * Proxy header protocol tcp4 value.
     */
    public static final String PROXY_HEADER_PROTOCOL_TCP4 = "TCP4";

    /**
     * Proxy header protocol tcp6 value.
     */
    public static final String PROXY_HEADER_PROTOCOL_TCP6 = "TCP6";

    /**
     * Proxy header protocol unknown value.
     */
    public static final String PROXY_HEADER_PROTOCOL_UNKNOWN = "UNKNOWN";

    /**
     * Proxy header reader max line bytes value.
     */
    public static final int PROXY_HEADER_READER_MAX_LINE_BYTES = 108;

    /**
     * Proxy plan direct id value.
     */
    public static final String PROXY_PLAN_DIRECT_ID = "direct";

    /**
     * Public suffix public suffix resource value.
     */
    public static final String PUBLIC_SUFFIX_PUBLIC_SUFFIX_RESOURCE = "suffixes.gz";

    /**
     * Default TLS protocol versions.
     */
    public static final List<String> TLS_SETTINGS_DEFAULT_VERSIONS = List
            .of(TlsVersion.TLSv1_3.javaName(), TlsVersion.TLSv1_2.javaName());

    /**
     * Maximum encrypted or plaintext bytes retained by one TLS staging buffer.
     */
    public static final long TLS_MAX_STAGING_BUFFER_BYTES = Normal.MEBI;

    /**
     * Meter event observer duration value.
     */
    public static final String METER_EVENT_OBSERVER_DURATION = ".duration";

    /**
     * Meter event observer failure value.
     */
    public static final String METER_EVENT_OBSERVER_FAILURE = "failure";

    /**
     * Rolling window nanos per second value.
     */
    public static final double ROLLING_WINDOW_NANOS_PER_SECOND = Normal.GIGA;

    /**
     * Cookie max date millis value.
     */
    public static final long COOKIE_MAX_DATE_MILLIS = 253402300799999L;

    /**
     * Demuxer default channel header value.
     */
    public static final String DEMUXER_DEFAULT_CHANNEL_HEADER = "X-Fabric-Channel";

    /**
     * Http2 connection window update threshold value.
     */
    public static final long HTTP2_CONNECTION_WINDOW_UPDATE_THRESHOLD = HTTP.DEFAULT_INITIAL_WINDOW_SIZE / Normal._2;

    /**
     * Http2 priority exclusive mask value.
     */
    public static final int HTTP2_PRIORITY_EXCLUSIVE_MASK = Integer.MIN_VALUE;

    /**
     * Http2 stream default window value.
     */
    public static final long HTTP2_STREAM_DEFAULT_WINDOW = HTTP.DEFAULT_INITIAL_WINDOW_SIZE;

    /**
     * Default browser version pattern suffix.
     */
    public static final String HTTP_AGENT_BROWSER_OTHER_VERSION = "[\\/ ]([\\d\\w\\.\\-]+)";

    /**
     * Unknown browser.
     */
    public static final Browser HTTP_AGENT_BROWSER_UNKNOWN = new Browser(Normal.UNKNOWN, null, null);

    /**
     * Unknown client operating system.
     */
    public static final ClientOs HTTP_AGENT_CLIENT_OS_UNKNOWN = new ClientOs(Normal.UNKNOWN, null);

    /**
     * Android device classifier.
     */
    public static final Device HTTP_AGENT_DEVICE_ANDROID = new Device("Android", "android");

    /**
     * Google TV device classifier.
     */
    public static final Device HTTP_AGENT_DEVICE_GOOGLE_TV = new Device("GoogleTV", "googletv");

    /**
     * HarmonyOS device classifier.
     */
    public static final Device HTTP_AGENT_DEVICE_HARMONY = new Device("Harmony", "OpenHarmony");

    /**
     * iPad device classifier.
     */
    public static final Device HTTP_AGENT_DEVICE_IPAD = new Device("iPad", "ipad");

    /**
     * iPhone device classifier.
     */
    public static final Device HTTP_AGENT_DEVICE_IPHONE = new Device("iPhone", "iphone");

    /**
     * iPod device classifier.
     */
    public static final Device HTTP_AGENT_DEVICE_IPOD = new Device("iPod", "ipod");

    /**
     * Unknown device.
     */
    public static final Device HTTP_AGENT_DEVICE_UNKNOWN = new Device(Normal.UNKNOWN, null);

    /**
     * Windows Phone device classifier.
     */
    public static final Device HTTP_AGENT_DEVICE_WINDOWS_PHONE = new Device("Windows Phone",
            "windows (ce|phone|mobile)( os)?");

    /**
     * Unknown browser engine.
     */
    public static final Engine HTTP_AGENT_ENGINE_UNKNOWN = new Engine(Normal.UNKNOWN, null);

    /**
     * Http auth basic value.
     */
    public static final String HTTP_AUTH_BASIC = "Basic";

    /**
     * Http auth basic lower value.
     */
    public static final String HTTP_AUTH_BASIC_LOWER = "basic";

    /**
     * Http cache codec meta code value.
     */
    public static final String HTTP_CACHE_CODEC_META_CODE = "Fabric-Http-Code";

    /**
     * Http cache codec meta media value.
     */
    public static final String HTTP_CACHE_CODEC_META_MEDIA = "Fabric-Http-Media";

    /**
     * Http cache codec meta message value.
     */
    public static final String HTTP_CACHE_CODEC_META_MESSAGE = "Fabric-Http-Message";

    /**
     * Http cache codec meta method value.
     */
    public static final String HTTP_CACHE_CODEC_META_METHOD = "Fabric-Http-Method";

    /**
     * Http cache codec meta protocol value.
     */
    public static final String HTTP_CACHE_CODEC_META_PROTOCOL = "Fabric-Cache-Protocol";

    /**
     * Http cache codec meta protocol http value.
     */
    public static final String HTTP_CACHE_CODEC_META_PROTOCOL_HTTP = Protocol.HTTP.name;

    /**
     * Http cache codec meta received at value.
     */
    public static final String HTTP_CACHE_CODEC_META_RECEIVED_AT = "Fabric-Http-Received-At";

    /**
     * Http cache codec meta request header name value.
     */
    public static final String HTTP_CACHE_CODEC_META_REQUEST_HEADER_NAME = "Fabric-Http-Request-Header-Name";

    /**
     * Http cache codec meta request header value value.
     */
    public static final String HTTP_CACHE_CODEC_META_REQUEST_HEADER_VALUE = "Fabric-Http-Request-Header-Value";

    /**
     * Http cache codec meta response header name value.
     */
    public static final String HTTP_CACHE_CODEC_META_RESPONSE_HEADER_NAME = "Fabric-Http-Response-Header-Name";

    /**
     * Http cache codec meta response header value value.
     */
    public static final String HTTP_CACHE_CODEC_META_RESPONSE_HEADER_VALUE = "Fabric-Http-Response-Header-Value";

    /**
     * Http cache codec meta response protocol value.
     */
    public static final String HTTP_CACHE_CODEC_META_RESPONSE_PROTOCOL = "Fabric-Http-Response-Protocol";

    /**
     * Http cache codec meta sent at value.
     */
    public static final String HTTP_CACHE_CODEC_META_SENT_AT = "Fabric-Http-Sent-At";

    /**
     * Http cache codec meta url value.
     */
    public static final String HTTP_CACHE_CODEC_META_URL = "Fabric-Http-Url";

    /**
     * Http connect socks5 value.
     */
    public static final byte HTTP_CONNECT_SOCKS5 = 0x05;

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
     * Multipart body boundary parameter value.
     */
    public static final String MULTIPART_BODY_BOUNDARY_PARAMETER = "boundary";

    /**
     * Soap body soap namespace value.
     */
    public static final String SOAP_BODY_SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * Soap body soap prefix value.
     */
    public static final String SOAP_BODY_SOAP_PREFIX = "soap";

    /**
     * Soap x header namespace value.
     */
    public static final String SOAP_X_HEADER_NAMESPACE = "urn:bus:fabric:soap:header";

    /**
     * Soap x header prefix value.
     */
    public static final String SOAP_X_HEADER_PREFIX = "h";

    /**
     * Socket server accept activity name.
     */
    public static final String SOCKET_ACTIVITY_ACCEPT = "socket-server-accept";

    /**
     * Socket server message activity name.
     */
    public static final String SOCKET_ACTIVITY_MESSAGE = "socket-server-message";

    /**
     * Socket server read activity name.
     */
    public static final String SOCKET_ACTIVITY_READ = "socket-server-read";

    /**
     * Socket open tag.
     */
    public static final String SOCKET_TAG_OPEN = "socket-open";

    /**
     * Socket x kcp scheme value.
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
     * Sse body data prefix value.
     */
    public static final String SSE_BODY_DATA_PREFIX = "data: ";

    /**
     * Sse body event prefix value.
     */
    public static final String SSE_BODY_EVENT_PREFIX = "event: ";

    /**
     * Sse body id prefix value.
     */
    public static final String SSE_BODY_ID_PREFIX = "id: ";

    /**
     * Sse body retry prefix value.
     */
    public static final String SSE_BODY_RETRY_PREFIX = "retry: ";

    /**
     * SSE default event name.
     */
    public static final String SSE_DEFAULT_EVENT = "message";

    /**
     * Sse retry default current value.
     */
    public static final Duration SSE_RETRY_DEFAULT_CURRENT = Duration.ofSeconds(Normal._3);

    /**
     * Sse retry default max delay value.
     */
    public static final Duration SSE_RETRY_DEFAULT_MAX_DELAY = Duration.ofSeconds(Normal._30);

    /**
     * Sse runner dispatch prefix value.
     */
    public static final String SSE_RUNNER_DISPATCH_PREFIX = "sse:" + Symbol.FORWARDSLASH;

    /**
     * Sse runner last event id value.
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
    public static final String STOMP_QUEUE_PREFIX = "/queue";

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
     * Stomp topic multi level wildcard value.
     */
    public static final String STOMP_TOPIC_MULTI_LEVEL_WILDCARD = Symbol.SLASH + Symbol.STAR + Symbol.STAR;

    /**
     * STOMP topic destination prefix.
     */
    public static final String STOMP_TOPIC_PREFIX = "/topic";

    /**
     * Stomp topic single level wildcard value.
     */
    public static final String STOMP_TOPIC_SINGLE_LEVEL_WILDCARD = Symbol.SLASH + Symbol.STAR;

    /**
     * STOMP 1.2 protocol version.
     */
    public static final String STOMP_VERSION_1_2 = "1.2";

    /**
     * WebSocket server accept activity name.
     */
    public static final String WEBSOCKET_ACTIVITY_ACCEPT = "websocket-server-accept";

    /**
     * WebSocket close-timeout activity name.
     */
    public static final String WEBSOCKET_ACTIVITY_CLOSE_TIMEOUT = "websocket-close-timeout";

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
     * WebSocket outbound queue full close reason.
     */
    public static final String WEBSOCKET_QUEUE_FULL_REASON = "queue full";

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
     * WebSocket write tag.
     */
    public static final String WEBSOCKET_WRITE = "websocket-write";

    /**
     * Web socket frame min text code point value.
     */
    public static final char WEB_SOCKET_FRAME_MIN_TEXT_CODE_POINT = 0x20;

    /**
     * Web socket reader default address value.
     */
    public static final String WEB_SOCKET_READER_DEFAULT_ADDRESS = Protocol.WS_PREFIX + "localhost";

    /**
     * Web socket session materialize send payload value.
     */
    public static final String WEB_SOCKET_SESSION_MATERIALIZE_SEND_PAYLOAD = "WebSocketSession.send(Payload)";

    /**
     * Directory connection value.
     */
    public static final String DIRECTORY_CONNECTION = "connection";

    /**
     * Directory policy value.
     */
    public static final String DIRECTORY_POLICY = "policy";

    /**
     * Directory proxy value.
     */
    public static final String DIRECTORY_PROXY = "proxy";

    /**
     * Directory resolver value.
     */
    public static final String DIRECTORY_RESOLVER = "resolver";

    /**
     * Selector max backoff value.
     */
    public static final Duration SELECTOR_MAX_BACKOFF = Duration.ofMinutes(Normal._5);

    /**
     * Lifecycle scope name value.
     */
    public static final String LIFECYCLE_SCOPE_NAME = "name";

}
