/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.support.mcp;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of McpClient for the STDIO protocol. It manages a local subprocess and communicates with it through
 * standard input/output.
 */
public class StdioClient implements McpClient {

    private final Assets assets;
    private Process process;
    private BufferedWriter writer;
    private Thread readerThread;
    private List<Tool> tools;

    /**
     * A thread-safe map to hold pending requests, waiting for a response from the subprocess. The key is the unique
     * request ID, and the value is a Sink that the caller can subscribe to.
     */
    private final Map<String, Sinks.One<String>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Constructs a new StdioClient.
     * 
     * @param assets The Assets configuration, must contain "command" and "args" (as a JSON string).
     */
    public StdioClient(Assets assets) {
        this.assets = assets;
    }

    @Override
    public Mono<Void> initialize() {
        return Mono.fromRunnable(() -> {
            String command = assets.getCommand();
            // Assuming assets.getArgs() returns a JSON string representing a List<String>
            List<String> args = null;
            if (StringKit.isNotEmpty(assets.getArgs())) {
                args = JsonKit.toList(assets.getArgs(), String.class);
            }

            if (StringKit.isEmpty(command) || args == null) {
                throw new IllegalArgumentException("Stdio assets must contain 'command' and 'args'");
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command).command(args);
            try {
                Logger.info(
                        "Starting stdio process for asset {}: {}",
                        assets.getName(),
                        String.join(" ", processBuilder.command()));
                this.process = processBuilder.start();
                this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

                // Start a dedicated thread to read from the process's stdout
                this.readerThread = new Thread(this::readProcessOutput);
                this.readerThread.setDaemon(true);
                this.readerThread.setName("mcp-stdio-reader-" + assets.getName());
                this.readerThread.start();

                // In a real implementation, listTools would be an async call itself.
                // We simulate this by calling it and blocking for the result during initialization.
                this.tools = listTools().block(Duration.ofMillis(assets.getTimeout()));
                Logger.info("Stdio process for asset {} started and tools listed successfully.", assets.getName());

            } catch (IOException e) {
                Logger.error("Failed to start stdio process for asset {}", assets.getName(), e);
                throw new RuntimeException("Failed to start stdio process", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * A long-running task that continuously reads from the process's standard output.
     */
    private void readProcessOutput() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Logger.trace("Received from stdio for asset {}: {}", assets.getName(), line);
                // Assuming each line is a JSON response
                Map<String, Object> response = JsonKit.toMap(line);
                String requestId = (String) response.get("requestId");
                if (requestId != null) {
                    Sinks.One<String> sink = pendingRequests.remove(requestId);
                    if (sink != null) {
                        // Check for error in response
                        if (response.containsKey("error")) {
                            sink.tryEmitError(new RuntimeException("Error from tool: " + response.get("error")));
                        } else {
                            sink.tryEmitValue(JsonKit.toJsonString(response.get("result")));
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (!"Stream closed".equals(e.getMessage())) {
                Logger.error("Error reading from stdio process for asset {}", assets.getName(), e);
            }
        } finally {
            // If the reader thread exits, it means the process has terminated.
            // We should fail all pending requests.
            failAllPendingRequests(
                    new IOException(
                            "The stdio process for asset " + assets.getName() + " has terminated unexpectedly."));
        }
    }

    @Override
    public void close() {
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
        if (process != null && process.isAlive()) {
            Logger.info("Destroying stdio process for asset {}...", assets.getName());
            process.destroyForcibly();
        }
        failAllPendingRequests(new IOException("Client for asset " + assets.getName() + " is shutting down."));
    }

    @Override
    public List<Tool> getTools() {
        return tools != null ? tools : Collections.emptyList();
    }

    @Override
    public Mono<String> callTool(String toolName, Map<String, Object> arguments) {
        String requestId = ID.objectId();
        Sinks.One<String> sink = Sinks.one();
        pendingRequests.put(requestId, sink);

        return Mono.fromRunnable(() -> {
            try {
                if (!isHealthy().block()) { // Use the new health check method
                    throw new IllegalStateException("Stdio process for asset " + assets.getName() + " is not running.");
                }
                Map<String, Object> requestPayload = Map
                        .of("requestId", requestId, "action", "callTool", "toolName", toolName, "arguments", arguments);
                String jsonRequest = JsonKit.toJsonString(requestPayload);
                writer.write(jsonRequest + "\n");
                writer.flush();
                Logger.trace("Sent to stdio for asset {}: {}", assets.getName(), jsonRequest);
            } catch (IOException e) {
                // If we fail to send, immediately fail the Mono
                pendingRequests.remove(requestId);
                sink.tryEmitError(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then(sink.asMono().timeout(Duration.ofMillis(assets.getTimeout()))) // Add
                                                                                                                        // a
                                                                                                                        // timeout
                                                                                                                        // for
                                                                                                                        // the
                                                                                                                        // response
                .doFinally(signalType -> pendingRequests.remove(requestId)); // Clean up in case of cancellation or
                                                                             // timeout
    }

    /**
     * Checks if the underlying subprocess is still alive.
     * 
     * @return A Mono emitting true if the process is alive, false otherwise.
     */
    @Override
    public Mono<Boolean> isHealthy() {
        return Mono.just(process != null && process.isAlive());
    }

    /**
     * Simulates a listTools call to the subprocess. In a real implementation, this would be an async call like
     * callTool.
     */
    private Mono<List<Tool>> listTools() {
        // This is a simplified simulation. A real implementation would use the same
        // request-response mechanism as callTool.
        return Mono.fromCallable(() -> {
            Logger.info("Simulating listTools call to stdio process for asset {}...", assets.getName());
            // In a real scenario, you'd send a listTools request and wait for the response.
            // For now, we return a hardcoded list.
            return List.of(new Tool("local_exec", "Executes a local command on the server", Collections.emptyMap()));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Fails all pending requests with a given exception. This is called when the connection is lost.
     * 
     * @param e The exception to fail the requests with.
     */
    private void failAllPendingRequests(Exception e) {
        for (Sinks.One<String> sink : pendingRequests.values()) {
            sink.tryEmitError(e);
        }
        pendingRequests.clear();
    }
}
