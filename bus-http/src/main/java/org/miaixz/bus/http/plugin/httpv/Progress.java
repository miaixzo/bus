/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.plugin.httpv;

/**
 * Represents the progress of an upload or download operation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Progress {

    /**
     * The default step size in bytes for progress updates.
     */
    public static final int DEFAULT_STEP_BYTES = 8192;

    /**
     * The total number of bytes to be transferred.
     */
    private long totalBytes;

    /**
     * The number of bytes that have been transferred so far.
     */
    private long doneBytes;

    /**
     * Constructs a new {@code Progress} instance.
     *
     * @param totalBytes The total number of bytes.
     * @param doneBytes  The number of bytes already completed.
     */
    public Progress(long totalBytes, long doneBytes) {
        this.totalBytes = totalBytes;
        this.doneBytes = doneBytes;
    }

    /**
     * Returns the progress rate as a value between 0.0 and 1.0.
     *
     * @return The progress rate.
     */
    public double getRate() {
        return (double) doneBytes / totalBytes;
    }

    /**
     * Returns the total number of bytes.
     *
     * @return The total number of bytes.
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * Returns the number of bytes that have been completed.
     *
     * @return The number of completed bytes.
     */
    public long getDoneBytes() {
        return doneBytes;
    }

    /**
     * Returns whether the operation is complete.
     *
     * @return {@code true} if the operation is complete, {@code false} otherwise.
     */
    public boolean isDone() {
        return doneBytes >= totalBytes;
    }

    /**
     * Adds a delta to the number of completed bytes.
     *
     * @param delta The number of bytes to add.
     */
    public void addDoneBytes(long delta) {
        doneBytes += delta;
    }

    /**
     * Increments the number of completed bytes by one.
     */
    public void increaseDoneBytes() {
        doneBytes++;
    }

    /**
     * Returns whether the operation is not yet done or has not reached a certain number of bytes.
     *
     * @param bytes The threshold in bytes.
     * @return {@code true} if the operation is not done or has not reached the threshold.
     */
    public boolean notDoneOrReached(long bytes) {
        return doneBytes < bytes && doneBytes < totalBytes;
    }

}
