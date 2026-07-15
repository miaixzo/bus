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
package org.miaixz.bus.fabric.cache;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.FaultHideSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.ResourceScope;
import org.miaixz.bus.logger.Logger;

/**
 * A cache that uses a limited amount of space on a filesystem. Each cache entry has a string key and a fixed number of
 * values. Each key must match the regex {@code [a-z0-9_-]{1,64}}. Values are byte sequences, accessible as streams or
 * files.
 * <p>
 * Each value must be between {@code 0} and {@code Integer.MAX_VALUE} bytes in length.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DiskLruCache implements Closeable, Flushable {

    /**
     * Primary journal file containing replayable cache mutations.
     */
    static final String JOURNAL_FILE = "journal";

    /**
     * Temporary journal file used while rewriting the primary journal atomically.
     */
    static final String JOURNAL_FILE_TEMP = "journal.tmp";

    /**
     * Backup journal file used to recover from an interrupted rewrite.
     */
    static final String JOURNAL_FILE_BACKUP = "journal.bkp";

    /**
     * Header marker that identifies files owned by this cache implementation.
     */
    static final String MAGIC = "libcore.io.DiskLruCache";

    /**
     * Journal format version supported by this cache.
     */
    static final String VERSION_1 = Symbol.ONE;

    /**
     * Sequence guard used when an edit is allowed regardless of the current committed snapshot.
     */
    static final long ANY_SEQUENCE_NUMBER = -1;

    /**
     * Key pattern written into journal lines and mapped to cache value files.
     */
    static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,120}");

    /**
     * Filesystem abstraction used by tests and alternate cache storage implementations.
     */
    final DiskFile diskFile;

    /**
     * The directory where the cache stores its data.
     */
    final File directory;

    /**
     * Number of value files maintained for each logical cache entry.
     */
    final int valueCount;

    /**
     * Access-ordered entry map that drives least-recently-used eviction.
     */
    final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap<>(0, Normal.DEFAULT_LOAD_FACTOR, true);

    /**
     * Primary journal path inside {@link #directory}.
     */
    private final File journalFile;

    /**
     * Temporary journal path used during journal rebuilds.
     */
    private final File journalFileTmp;

    /**
     * Backup journal path used when replacing the primary journal.
     */
    private final File journalFileBackup;

    /**
     * Application version persisted in the journal header to invalidate incompatible cache contents.
     */
    private final int appVersion;

    /**
     * Executor used for eviction and journal compaction after write operations.
     */
    private final Executor executor;

    /**
     * Optional owner of background cleanup resources created by {@link #create(DiskFile, File, int, int, long)}.
     */
    private final AutoCloseable cleanupCloseable;

    /**
     * Buffered writer appending mutations to the active journal.
     */
    BufferSink journalWriter;

    /**
     * Count of journal operations that no longer change live cache state and can trigger compaction.
     */
    int redundantOpCount;

    /**
     * True after journal append failures; new edits are rejected until the cache is rebuilt.
     */
    boolean hasJournalErrors;

    /**
     * True after the journal has been replayed or rebuilt.
     */
    boolean initialized;

    /**
     * True if the cache is closed.
     */
    boolean closed;

    /**
     * True when the most recent background trim failed and should be retried by a later operation.
     */
    boolean mostRecentTrimFailed;

    /**
     * True when the most recent journal rebuild failed and the journal must be compacted later.
     */
    boolean mostRecentRebuildFailed;

    /**
     * The maximum number of bytes this cache should use to store its data.
     */
    private long maxSize;

    /**
     * The number of bytes currently being used to store the values in this cache.
     */
    private long size = 0;

    /**
     * To differentiate between stale and current snapshots, each entry is given a sequence number when an edit is
     * committed. A snapshot is stale if its sequence number is not equal to its entry's sequence number.
     */
    private long nextSequenceNumber = 0;
    /**
     * Background task that trims the journal and evicts entries after writes.
     */
    private final Runnable cleanupRunnable = new Runnable() {

        /**
         * Runs deferred cache trimming and journal rebuild work.
         */
        @Override
        public void run() {
            synchronized (DiskLruCache.this) {
                if (!initialized || closed) {
                    return; // Nothing to do.
                }

                try {
                    trimToSize();
                } catch (IOException ignored) {
                    Logger.warn(
                            false,
                            "Http",
                            ignored,
                            "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                            "DiskLruCache",
                            true,
                            ignored.getClass().getSimpleName());
                    mostRecentTrimFailed = true;
                }

                try {
                    if (journalRebuildRequired()) {
                        rebuildJournal();
                        redundantOpCount = 0;
                    }
                } catch (IOException e) {
                    Logger.warn(
                            false,
                            "Http",
                            e,
                            "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                            "DiskLruCache",
                            true,
                            e.getClass().getSimpleName());
                    mostRecentRebuildFailed = true;
                    journalWriter = IoKit.buffer(IoKit.blackhole());
                }
            }
        }
    };

    /**
     * Creates a cache with externally managed cleanup execution.
     *
     * @param diskFile   filesystem abstraction
     * @param directory  cache directory
     * @param appVersion application cache version
     * @param valueCount number of files per entry
     * @param maxSize    maximum retained bytes
     * @param executor   cleanup executor
     */
    DiskLruCache(DiskFile diskFile, File directory, int appVersion, int valueCount, long maxSize, Executor executor) {
        this(diskFile, directory, appVersion, valueCount, maxSize, executor, null);
    }

    /**
     * Creates a cache and optionally records cleanup resources that should close with it.
     *
     * @param diskFile         filesystem abstraction
     * @param directory        cache directory
     * @param appVersion       application cache version
     * @param valueCount       number of files per entry
     * @param maxSize          maximum retained bytes
     * @param executor         cleanup executor
     * @param cleanupCloseable cleanup owner, or {@code null}
     */
    DiskLruCache(DiskFile diskFile, File directory, int appVersion, int valueCount, long maxSize, Executor executor,
                 AutoCloseable cleanupCloseable) {
        this.diskFile = diskFile;
        this.directory = directory;
        this.appVersion = appVersion;
        this.journalFile = new File(directory, JOURNAL_FILE);
        this.journalFileTmp = new File(directory, JOURNAL_FILE_TEMP);
        this.journalFileBackup = new File(directory, JOURNAL_FILE_BACKUP);
        this.valueCount = valueCount;
        this.maxSize = maxSize;
        this.executor = executor;
        this.cleanupCloseable = cleanupCloseable;
    }

    /**
     * Creates a cache that resides in {@code directory}. This cache is lazily initialized on first access and will be
     * created if it does not exist.
     *
     * @param diskFile   the file system to use.
     * @param directory  a writable directory.
     * @param appVersion the version of the application using the cache.
     * @param valueCount the number of values per cache entry.
     * @param maxSize    the maximum number of bytes this cache should use to store its data.
     * @return the disk cache.
     */
    public static DiskLruCache create(DiskFile diskFile, File directory, int appVersion, int valueCount, long maxSize) {
        Assert.isTrue(maxSize > 0, "maxSize <= 0");
        Assert.isTrue(valueCount > 0, "valueCount <= 0");

        final ResourceScope scope = ResourceScope.create();
        final Dispatcher dispatcher = scope.add(Dispatcher.create());
        final Executor executor = command -> dispatcher.run("disk-lru-cache:cleanup", command);

        return new DiskLruCache(diskFile, directory, appVersion, valueCount, maxSize, executor, scope);
    }

    /**
     * Initializes the cache. This will include reading the journal file from storage and building up the necessary
     * in-memory cache information. Note that if the application chooses not to call this method to initialize the
     * cache, it will be initialized lazily on the first use of the cache.
     *
     * @throws IOException if an I/O error occurs during initialization.
     */
    public synchronized void initialize() throws IOException {
        assert Thread.holdsLock(this);

        if (initialized) {
            return; // Already initialized.
        }

        // If a backup file exists, use it.
        if (diskFile.exists(journalFileBackup)) {
            // If a journal file also exists, delete the backup file.
            if (diskFile.exists(journalFile)) {
                diskFile.delete(journalFileBackup);
            } else {
                diskFile.rename(journalFileBackup, journalFile);
            }
        }

        // Replay the journal file to restore the cache to a consistent state.
        if (diskFile.exists(journalFile)) {
            try {
                readJournal();
                processJournal();
                initialized = true;
                return;
            } catch (IOException journalIsCorrupt) {
                Logger.warn(
                        false,
                        "Http",
                        "DiskLruCache journal corrupt: protocol=http, directory=" + directory + ", message="
                                + journalIsCorrupt.getMessage() + ", removing",
                        journalIsCorrupt);
            }

            // The cache is corrupted, attempt to delete the contents of the directory. This can throw and
            // we'll let that propagate out as it likely means there is a severe filesystem problem.
            try {
                delete();
            } finally {
                closed = false;
            }
        }

        rebuildJournal();

        initialized = true;
    }

    /**
     * Replays the current journal into memory and prepares the append writer.
     * <p>
     * A truncated tail is treated as recoverable: live entries are kept and the journal is rebuilt before accepting
     * more mutations.
     * </p>
     *
     * @throws IOException when the journal header or persisted operations are invalid
     */
    private void readJournal() throws IOException {
        try (BufferSource source = IoKit.buffer(diskFile.source(journalFile))) {
            Journal.readHeader(source, appVersion, valueCount);

            int lineCount = 0;
            while (true) {
                try {
                    readJournalLine(source.readUtf8LineStrict());
                    lineCount++;
                } catch (EOFException endOfJournal) {
                    Logger.warn(
                            false,
                            "Http",
                            endOfJournal,
                            "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                            "DiskLruCache",
                            true,
                            endOfJournal.getClass().getSimpleName());
                    break;
                }
            }
            redundantOpCount = lineCount - lruEntries.size();

            // If we ended with a truncated line, rebuild the journal before appending to it.
            if (!source.exhausted()) {
                rebuildJournal();
            } else {
                journalWriter = newJournalWriter();
            }
        }
    }

    /**
     * Applies one journal mutation line to the in-memory entry map.
     *
     * @param line raw journal line
     * @throws IOException when the line cannot be parsed as a known journal operation
     */
    private void readJournalLine(String line) throws IOException {
        final Journal.Line parsed = Journal.parseLine(line);
        if (parsed.remove()) {
            lruEntries.remove(parsed.key());
            return;
        }

        Entry entry = lruEntries.get(parsed.key());
        if (null == entry) {
            entry = new Entry(parsed.key());
            lruEntries.put(parsed.key(), entry);
        }

        if (parsed.clean()) {
            entry.readable = true;
            entry.currentEditor = null;
            entry.setLengths(parsed.lengths());
        } else if (parsed.dirty()) {
            entry.currentEditor = new Editor(entry);
        } else if (parsed.read()) {
            // This work was already done by calling lruEntries.get().
        } else {
            throw new IOException("unexpected journal line: " + line);
        }
    }

    /**
     * Opens the active journal for append and converts write failures into cache state.
     *
     * @return buffered journal writer
     * @throws FileNotFoundException when the journal file cannot be opened
     */
    private BufferSink newJournalWriter() throws FileNotFoundException {
        Sink fileSink = diskFile.appendingSink(journalFile);
        Sink faultHidingSink = new FaultHideSink(fileSink) {

            /**
             * Called when an I/O exception occurs during journal write operations.
             * <p>
             * This method sets a flag to indicate that journal errors have occurred, which prevents the cache from
             * being used until it is deleted and recreated.
             * </p>
             *
             * @param e The {@link IOException} that occurred.
             */
            @Override
            protected void onException(IOException e) {
                assert (Thread.holdsLock(DiskLruCache.this));
                hasJournalErrors = true;
            }
        };
        return IoKit.buffer(faultHidingSink);
    }

    /**
     * Computes the initial size and collects garbage as a part of opening the cache. Dirty entries are assumed to be
     * inconsistent and will be deleted.
     *
     * @throws IOException if an I/O error occurs.
     */
    private void processJournal() throws IOException {
        diskFile.delete(journalFileTmp);
        for (Iterator<Entry> i = lruEntries.values().iterator(); i.hasNext(); ) {
            Entry entry = i.next();
            if (entry.currentEditor == null) {
                for (int t = 0; t < valueCount; t++) {
                    size += entry.lengths[t];
                }
            } else {
                entry.currentEditor = null;
                for (int t = 0; t < valueCount; t++) {
                    diskFile.delete(entry.cleanFiles[t]);
                    diskFile.delete(entry.dirtyFiles[t]);
                }
                i.remove();
            }
        }
    }

    /**
     * Creates a new journal that omits redundant information. If a current journal exists, it will be replaced.
     *
     * @throws IOException if an I/O error occurs.
     */
    synchronized void rebuildJournal() throws IOException {
        if (null != journalWriter) {
            journalWriter.close();
        }

        try (BufferSink writer = IoKit.buffer(diskFile.sink(journalFileTmp))) {
            Journal.writeHeader(writer, appVersion, valueCount);

            for (Entry entry : lruEntries.values()) {
                if (null != entry.currentEditor) {
                    Journal.writeDirty(writer, entry.key);
                } else {
                    Journal.writeClean(writer, entry);
                }
            }
        }

        if (diskFile.exists(journalFile)) {
            diskFile.rename(journalFile, journalFileBackup);
        }
        diskFile.rename(journalFileTmp, journalFile);
        diskFile.delete(journalFileBackup);

        journalWriter = newJournalWriter();
        hasJournalErrors = false;
        mostRecentRebuildFailed = false;
    }

    /**
     * Returns a snapshot of the entry named {@code key}, or null if it doesn't exist or is not currently readable. If a
     * value is returned, it will be moved to the head of the LRU queue.
     *
     * @param key the cache key.
     * @return the snapshot.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized Snapshot get(String key) throws IOException {
        initialize();

        checkNotClosed();
        validateKey(key);
        Entry entry = lruEntries.get(key);
        if (null == entry || !entry.readable) {
            return null;
        }

        Snapshot snapshot = entry.snapshot();
        if (null == snapshot) {
            return null;
        }

        redundantOpCount++;
        Journal.writeRead(journalWriter, key);
        if (journalRebuildRequired()) {
            executor.execute(cleanupRunnable);
        }

        return snapshot;
    }

    /**
     * Returns an editor for the entry named {@code key}, or null if another edit is in progress.
     *
     * @param key the file key.
     * @return the editor.
     * @throws IOException if an I/O error occurs.
     */
    public Editor edit(String key) throws IOException {
        return edit(key, ANY_SEQUENCE_NUMBER);
    }

    /**
     * Opens an editor only when the caller's expected snapshot sequence still matches the entry.
     *
     * @param key                    entry key
     * @param expectedSequenceNumber expected committed sequence, or {@link #ANY_SEQUENCE_NUMBER}
     * @return editor, or {@code null} when the entry is stale, locked, or temporarily unhealthy
     * @throws IOException when journal access fails
     */
    synchronized Editor edit(String key, long expectedSequenceNumber) throws IOException {
        initialize();

        checkNotClosed();
        validateKey(key);
        Entry entry = lruEntries.get(key);
        if (expectedSequenceNumber != ANY_SEQUENCE_NUMBER
                && (null == entry || entry.sequenceNumber != expectedSequenceNumber)) {
            return null; // Snapshot is stale.
        }
        if (null != entry && null != entry.currentEditor) {
            return null; // Another edit is in progress.
        }
        if (mostRecentTrimFailed || mostRecentRebuildFailed) {
            // The cache is in a bad state. Let the cleanup task run before creating a new edit.
            executor.execute(cleanupRunnable);
            return null;
        }

        // Flush the journal before creating files to prevent file leaks.
        Journal.writeDirty(journalWriter, key);
        journalWriter.flush();

        if (hasJournalErrors) {
            return null; // Don't edit; the journal can't be written.
        }

        if (entry == null) {
            entry = new Entry(key);
            lruEntries.put(key, entry);
        }
        Editor editor = new Editor(entry);
        entry.currentEditor = editor;
        return editor;
    }

    /**
     * Returns the directory where this cache stores its data.
     *
     * @return the cache directory.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Returns the maximum number of bytes that this cache should use to store its data.
     *
     * @return the maximum size in bytes.
     */
    public synchronized long getMaxSize() {
        return maxSize;
    }

    /**
     * Changes the maximum number of bytes the cache can store and queues a job to trim the existing store, if
     * necessary.
     *
     * @param maxSize the new maximum size in bytes.
     */
    public synchronized void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
        if (initialized) {
            executor.execute(cleanupRunnable);
        }
    }

    /**
     * Returns the number of bytes currently being used to store the values in this cache. This may be greater than the
     * max size if a background deletion is pending.
     *
     * @return the current size in bytes.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized long size() throws IOException {
        initialize();
        return size;
    }

    /**
     * Publishes or aborts an editor and records the resulting mutation in the journal.
     *
     * @param editor  active editor
     * @param success true to publish dirty files as clean values
     * @throws IOException when filesystem or journal updates fail
     */
    synchronized void completeEdit(Editor editor, boolean success) throws IOException {
        Entry entry = editor.entry;
        if (entry.currentEditor != editor) {
            throw new IllegalStateException();
        }

        // If this edit is creating the entry for the first time, every index must have a value.
        if (success && !entry.readable) {
            for (int i = 0; i < valueCount; i++) {
                if (!editor.written[i]) {
                    editor.abort();
                    throw new IllegalStateException("Newly created entry didn't create value for index " + i);
                }
                if (!diskFile.exists(entry.dirtyFiles[i])) {
                    editor.abort();
                    return;
                }
            }
        }

        for (int i = 0; i < valueCount; i++) {
            File dirty = entry.dirtyFiles[i];
            if (success) {
                if (diskFile.exists(dirty)) {
                    File clean = entry.cleanFiles[i];
                    diskFile.rename(dirty, clean);
                    long oldLength = entry.lengths[i];
                    long newLength = diskFile.size(clean);
                    entry.lengths[i] = newLength;
                    size = size - oldLength + newLength;
                }
            } else {
                diskFile.delete(dirty);
            }
        }

        redundantOpCount++;
        entry.currentEditor = null;
        if (entry.readable || success) {
            entry.readable = true;
            Journal.writeClean(journalWriter, entry);
            if (success) {
                entry.sequenceNumber = nextSequenceNumber++;
            }
        } else {
            lruEntries.remove(entry.key);
            Journal.writeRemove(journalWriter, entry.key);
        }
        journalWriter.flush();

        if (size > maxSize || journalRebuildRequired()) {
            executor.execute(cleanupRunnable);
        }
    }

    /**
     * We only rebuild the journal when it will halve the size of the journal and eliminate at least 2000 ops.
     *
     * @return {@code true} if the journal needs to be rebuilt.
     */
    boolean journalRebuildRequired() {
        final int redundantOpCompactThreshold = 2000;
        return redundantOpCount >= redundantOpCompactThreshold && redundantOpCount >= lruEntries.size();
    }

    /**
     * Drops the entry for {@code key} if it exists and can be removed. If the entry for {@code key} is currently being
     * edited, that edit will complete normally but its value will not be stored.
     *
     * @param key the key of the entry to remove.
     * @return true if an entry was removed.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized boolean remove(String key) throws IOException {
        initialize();

        checkNotClosed();
        validateKey(key);
        Entry entry = lruEntries.get(key);
        if (entry == null)
            return false;
        boolean removed = removeEntry(entry);
        if (removed && size <= maxSize)
            mostRecentTrimFailed = false;
        return removed;
    }

    /**
     * Removes one entry's clean files and journal state.
     *
     * @param entry live entry to remove
     * @return {@code true} after removal has been recorded
     * @throws IOException when deleting files or writing the journal fails
     */
    boolean removeEntry(Entry entry) throws IOException {
        if (entry.currentEditor != null) {
            entry.currentEditor.detach(); // Prevent the edit from completing normally.
        }

        for (int i = 0; i < valueCount; i++) {
            diskFile.delete(entry.cleanFiles[i]);
            size -= entry.lengths[i];
            entry.lengths[i] = 0;
        }

        redundantOpCount++;
        Journal.writeRemove(journalWriter, entry.key);
        lruEntries.remove(entry.key);

        if (journalRebuildRequired()) {
            executor.execute(cleanupRunnable);
        }

        return true;
    }

    /**
     * Returns true if this cache has been closed.
     *
     * @return {@code true} if the cache is closed.
     */
    public synchronized boolean isClosed() {
        return closed;
    }

    /**
     * Guards public cache operations that require the journal writer to be usable.
     */
    private synchronized void checkNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException("cache is closed");
        }
    }

    /**
     * Force buffered operations to the filesystem.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public synchronized void flush() throws IOException {
        if (!initialized)
            return;

        checkNotClosed();
        trimToSize();
        journalWriter.flush();
    }

    /**
     * Closes this cache. Stored values will remain on the filesystem.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        synchronized (this) {
            if (!initialized || closed) {
                closed = true;
            } else {
                // Copying for safe iteration.
                for (Entry entry : lruEntries.values().toArray(new Entry[lruEntries.size()])) {
                    if (entry.currentEditor != null) {
                        entry.currentEditor.abort();
                    }
                }
                trimToSize();
                journalWriter.close();
                journalWriter = null;
                closed = true;
            }
        }
        closeCleanup();
    }

    /**
     * Closes cleanup resources outside the cache monitor.
     *
     * @throws IOException when cleanup resources fail to close
     */
    private void closeCleanup() throws IOException {
        if (cleanupCloseable == null) {
            return;
        }
        try {
            cleanupCloseable.close();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException("Unable to close DiskLruCache cleanup resources", e);
        }
    }

    /**
     * Evicts least-recently-used entries until the cache is within its configured byte budget.
     *
     * @throws IOException when entry removal fails
     */
    void trimToSize() throws IOException {
        while (size > maxSize) {
            Entry toEvict = lruEntries.values().iterator().next();
            removeEntry(toEvict);
        }
        mostRecentTrimFailed = false;
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete all files in the cache directory
     * including files that weren't created by the cache.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void delete() throws IOException {
        close();
        diskFile.deleteContents(directory);
    }

    /**
     * Deletes all stored values from the cache. In-flight edits will complete normally but their values will not be
     * stored.
     *
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void evictAll() throws IOException {
        initialize();
        // Copying for safe iteration.
        for (Entry entry : lruEntries.values().toArray(new Entry[lruEntries.size()])) {
            removeEntry(entry);
        }
        mostRecentTrimFailed = false;
    }

    /**
     * Validates a key before it is written into journal lines or mapped to value file names.
     *
     * @param key cache key
     */
    private void validateKey(String key) {
        Matcher matcher = LEGAL_KEY_PATTERN.matcher(key);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,120}: \"" + key + "\"");
        }
    }

    /**
     * Returns an iterator over the cache's current items. This iterator doesn't throw {@code
     * ConcurrentModificationException}, but callers must {@link Snapshot#close} each snapshot returned by
     * {@link Iterator#next}. Failing to do so will leak open files. The returned iterator supports
     * {@link Iterator#remove}.
     *
     * @return an iterator over the cache's current items.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized Iterator<Snapshot> snapshots() throws IOException {
        initialize();
        return new Iterator<>() {

            /**
             * A copy of the entries to iterate over to prevent concurrent modification errors.
             */
            final Iterator<Entry> delegate = new ArrayList<>(lruEntries.values()).iterator();

            /**
             * The snapshot to return from {@link #next}. Null if the snapshot hasn't been computed yet.
             */
            Snapshot nextSnapshot;

            /**
             * The snapshot to remove with {@link #remove}. Null if removal is illegal.
             */
            Snapshot removeSnapshot;

            /**
             * Returns {@code true} if there are more snapshots to iterate over.
             *
             * @return {@code true} if the iteration has more elements.
             */
            @Override
            public boolean hasNext() {
                if (nextSnapshot != null)
                    return true;

                synchronized (DiskLruCache.this) {
                    // If the cache is closed, truncate the iterator.
                    if (closed)
                        return false;

                    while (delegate.hasNext()) {
                        Entry entry = delegate.next();
                        if (!entry.readable)
                            continue; // Entry during edit.
                        Snapshot snapshot = entry.snapshot();
                        if (snapshot == null)
                            continue; // Evicted since we copied the entries.
                        nextSnapshot = snapshot;
                        return true;
                    }
                }

                return false;
            }

            /**
             * Returns the next snapshot in the iteration.
             *
             * @return The next {@link Snapshot}.
             * @throws NoSuchElementException if there are no more elements.
             */
            @Override
            public Snapshot next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                removeSnapshot = nextSnapshot;
                nextSnapshot = null;
                return removeSnapshot;
            }

            /**
             * Removes the current snapshot from the cache.
             *
             * @throws IllegalStateException if {@code remove()} is called before {@code next()}.
             */
            @Override
            public void remove() {
                if (removeSnapshot == null)
                    throw new IllegalStateException("remove() before next()");
                try {
                    DiskLruCache.this.remove(removeSnapshot.key);
                } catch (IOException ignored) {
                    Logger.warn(
                            false,
                            "Http",
                            ignored,
                            "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                            "DiskLruCache",
                            true,
                            ignored.getClass().getSimpleName());
                    // Nothing useful to do here. The cache may be corrupt but we don't know for sure.
                } finally {
                    removeSnapshot = null;
                }
            }
        };
    }

    /**
     * Access to read and write files on a hierarchical data store. Most callers should use the {@link #SYSTEM}
     * implementation, which uses the host machine's local file system. Alternate implementations may be used to inject
     * faults (for testing) or to transform stored data (to add encryption, for example).
     * <p>
     * All operations on a file system are racy. For example, guarding a call to {@link #source} with {@link #exists}
     * does not guarantee that {@link FileNotFoundException} will not be thrown. The file may be moved between the two
     * calls!
     * <p>
     * This interface is less ambitious than {@link java.nio.file.FileSystem} introduced in Java 7. It lacks important
     * features like file watching, metadata, permissions, and disk space information. In exchange for these
     * limitations, this interface is easier to implement and works on all versions of Java and Android.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public interface DiskFile {

        /**
         * The host machine's local file system.
         */
        DiskFile SYSTEM = Instances.get(DiskFile.class.getName() + ".system", () -> new DiskFile() {

            /**
             * Opens a source for a file.
             *
             * @param file source file
             * @return file source
             * @throws FileNotFoundException when the file cannot be opened
             */
            @Override
            public Source source(File file) throws FileNotFoundException {
                return IoKit.source(file);
            }

            /**
             * Opens a replacing sink for a file, creating the parent directory when needed.
             *
             * @param file target file
             * @return file sink
             * @throws FileNotFoundException when the file cannot be opened
             */
            @Override
            public Sink sink(File file) throws FileNotFoundException {
                try {
                    return IoKit.sink(file);
                } catch (FileNotFoundException e) {
                    Logger.warn(
                            false,
                            "Http",
                            e,
                            "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                            "DiskLruCache",
                            true,
                            e.getClass().getSimpleName());
                    // Maybe the parent directory doesn't exist? Try creating it first.
                    FileKit.mkdir(file.getParentFile());
                    return IoKit.sink(file);
                }
            }

            /**
             * Opens an appending sink for a file, creating the parent directory when needed.
             *
             * @param file target file
             * @return appending file sink
             * @throws FileNotFoundException when the file cannot be opened
             */
            @Override
            public Sink appendingSink(File file) throws FileNotFoundException {
                try {
                    return IoKit.appendingSink(file);
                } catch (FileNotFoundException e) {
                    Logger.warn(
                            false,
                            "Http",
                            e,
                            "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                            "DiskLruCache",
                            true,
                            e.getClass().getSimpleName());
                    // Maybe the parent directory doesn't exist? Try creating it first.
                    FileKit.mkdir(file.getParentFile());
                    return IoKit.appendingSink(file);
                }
            }

            /**
             * Deletes a file and treats existing undeleted files as failures.
             *
             * @param file file to delete
             * @throws IOException when deletion fails
             */
            @Override
            public void delete(File file) throws IOException {
                // If delete() fails, make sure it's because the file didn't exist!
                if (!file.delete() && file.exists()) {
                    throw new IOException("failed to delete " + file);
                }
            }

            /**
             * Returns whether a file exists.
             *
             * @param file file
             * @return true when the file exists
             */
            @Override
            public boolean exists(File file) {
                return file.exists();
            }

            /**
             * Returns the file length.
             *
             * @param file file
             * @return file length
             */
            @Override
            public long size(File file) {
                return file.length();
            }

            /**
             * Renames one file to another after removing the target.
             *
             * @param from source file
             * @param to   target file
             * @throws IOException when the rename fails
             */
            @Override
            public void rename(File from, File to) throws IOException {
                delete(to);
                if (!from.renameTo(to)) {
                    throw new IOException("failed to rename " + from + " to " + to);
                }
            }

            /**
             * Deletes all files below a directory.
             *
             * @param directory directory to clear
             * @throws IOException when the directory cannot be read or a child cannot be deleted
             */
            @Override
            public void deleteContents(File directory) throws IOException {
                File[] files = directory.listFiles();
                if (files == null) {
                    throw new IOException("not a readable directory: " + directory);
                }
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteContents(file);
                    }
                    if (!file.delete()) {
                        throw new IOException("failed to delete " + file);
                    }
                }
            }
        });

        /**
         * Reads from {@code file}.
         *
         * @param file the file to read from.
         * @return a source for the file.
         * @throws FileNotFoundException if the file does not exist.
         */
        Source source(File file) throws FileNotFoundException;

        /**
         * Writes to {@code file}, discarding any data already present. Creates parent directories if necessary.
         *
         * @param file the file to write to.
         * @return a sink for the file.
         * @throws FileNotFoundException if the file cannot be created.
         */
        Sink sink(File file) throws FileNotFoundException;

        /**
         * Writes to {@code file}, appending if data is already present. Creates parent directories if necessary.
         *
         * @param file the file to append to.
         * @return a sink for the file.
         * @throws FileNotFoundException if the file cannot be created.
         */
        Sink appendingSink(File file) throws FileNotFoundException;

        /**
         * Deletes {@code file} if it exists. Throws if the file exists and cannot be deleted.
         *
         * @param file the file to delete.
         * @throws IOException if the file cannot be deleted.
         */
        void delete(File file) throws IOException;

        /**
         * Returns true if {@code file} exists on the file system.
         *
         * @param file the file to check.
         * @return true if the file exists.
         */
        boolean exists(File file);

        /**
         * Returns the number of bytes stored in {@code file}, or 0 if it does not exist.
         *
         * @param file the file to measure.
         * @return the size of the file in bytes.
         */
        long size(File file);

        /**
         * Renames {@code from} to {@code to}. Throws if the file cannot be renamed.
         *
         * @param from the source file.
         * @param to   the destination file.
         * @throws IOException if the file cannot be renamed.
         */
        void rename(File from, File to) throws IOException;

        /**
         * Recursively delete the contents of {@code directory}. Throws an IOException if any file could not be deleted,
         * or if {@code dir} is not a readable directory.
         *
         * @param directory the directory to delete.
         * @throws IOException if the contents cannot be deleted.
         */
        void deleteContents(File directory) throws IOException;

    }

    /**
     * A snapshot of the values for an entry.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public final class Snapshot implements Closeable {

        /**
         * Entry key captured when this snapshot was opened.
         */
        private final String key;

        /**
         * Entry sequence used to reject stale snapshot edits.
         */
        private final long sequenceNumber;

        /**
         * Open value sources owned by this snapshot.
         */
        private final Source[] sources;

        /**
         * Committed byte lengths for {@link #sources}.
         */
        private final long[] lengths;

        /**
         * Creates a snapshot over committed value sources.
         *
         * @param key            entry key
         * @param sequenceNumber committed entry sequence
         * @param sources        value sources
         * @param lengths        value lengths
         */
        Snapshot(String key, long sequenceNumber, Source[] sources, long[] lengths) {
            this.key = key;
            this.sequenceNumber = sequenceNumber;
            this.sources = sources;
            this.lengths = lengths;
        }

        /**
         * Returns the key for this snapshot.
         *
         * @return the key.
         */
        public String key() {
            return key;
        }

        /**
         * Returns an editor for this snapshot's entry, or null if either the entry has changed since this snapshot was
         * created or if another edit is in progress.
         *
         * @return an editor or null.
         * @throws IOException if an I/O error occurs.
         */
        public Editor edit() throws IOException {
            return DiskLruCache.this.edit(key, sequenceNumber);
        }

        /**
         * Returns the unbuffered stream with the value for {@code index}.
         *
         * @param index the index of the value.
         * @return the source for the value.
         */
        public Source getSource(int index) {
            return sources[index];
        }

        /**
         * Returns the byte length of the value for {@code index}.
         *
         * @param index the index of the value.
         * @return the length of the value.
         */
        public long getLength(int index) {
            return lengths[index];
        }

        /**
         * Closes this snapshot.
         */
        public void close() {
            for (Source in : sources) {
                IoKit.close(in);
            }
        }

    }

    /**
     * Edits the values for an entry.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public final class Editor {

        /**
         * Entry currently locked by this editor.
         */
        final Entry entry;

        /**
         * Tracks which value indexes were written for a newly-created entry.
         */
        final boolean[] written;

        /**
         * True after this editor has committed, aborted, or been detached.
         */
        private boolean done;

        /**
         * Creates an editor and marks all values as unwritten for new entries.
         *
         * @param entry locked cache entry
         */
        Editor(Entry entry) {
            this.entry = entry;
            this.written = (entry.readable) ? null : new boolean[valueCount];
        }

        /**
         * Prevents this editor from completing normally. This is necessary either when the edit causes an I/O error, or
         * if the target entry is evicted while this editor is active. In either case we delete the editor's created
         * files and prevent new files from being created. Note that once an editor has been detached it is possible for
         * another editor to edit the entry.
         */
        void detach() {
            if (entry.currentEditor == this) {
                for (int i = 0; i < valueCount; i++) {
                    try {
                        diskFile.delete(entry.dirtyFiles[i]);
                    } catch (IOException e) {
                        Logger.warn(
                                false,
                                "Http",
                                e,
                                "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                                "DiskLruCache",
                                true,
                                e.getClass().getSimpleName());
                        // This file is potentially leaked. Not much we can do about that.
                    }
                }
                entry.currentEditor = null;
            }
        }

        /**
         * Returns an unbuffered input stream to read the last committed value, or null if no value has been committed.
         *
         * @param index the index of the value.
         * @return a source for the value, or null.
         */
        public Source newSource(int index) {
            synchronized (DiskLruCache.this) {
                if (done) {
                    throw new IllegalStateException();
                }
                if (!entry.readable || entry.currentEditor != this) {
                    return null;
                }
                try {
                    return diskFile.source(entry.cleanFiles[index]);
                } catch (FileNotFoundException e) {
                    Logger.warn(
                            false,
                            "Http",
                            e,
                            "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                            "DiskLruCache",
                            true,
                            e.getClass().getSimpleName());
                    return null;
                }
            }
        }

        /**
         * Returns a new unbuffered output stream to write the value at {@code index}. If the underlying output stream
         * encounters errors when writing to the filesystem, this edit will be aborted when {@link #commit} is called.
         * The returned output stream does not throw IOExceptions.
         *
         * @param index the index of the value.
         * @return a sink for the value.
         */
        public Sink newSink(int index) {
            synchronized (DiskLruCache.this) {
                if (done) {
                    throw new IllegalStateException();
                }
                if (entry.currentEditor != this) {
                    return IoKit.blackhole();
                }
                if (!entry.readable) {
                    written[index] = true;
                }
                File dirtyFile = entry.dirtyFiles[index];
                Sink sink;
                try {
                    sink = diskFile.sink(dirtyFile);
                } catch (FileNotFoundException e) {
                    Logger.warn(
                            false,
                            "Http",
                            e,
                            "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                            "DiskLruCache",
                            true,
                            e.getClass().getSimpleName());
                    return IoKit.blackhole();
                }
                return new FaultHideSink(sink) {

                    /**
                     * Called when an I/O exception occurs during write operations.
                     * <p>
                     * This method detaches the editor to prevent it from completing normally, which ensures that any
                     * partially written files are cleaned up.
                     * </p>
                     *
                     * @param e The {@link IOException} that occurred.
                     */
                    @Override
                    protected void onException(IOException e) {
                        synchronized (DiskLruCache.this) {
                            detach();
                        }
                    }
                };
            }
        }

        /**
         * Commits this edit so it is visible to readers. This releases the edit lock so another edit may be started on
         * the same key.
         *
         * @throws IOException if an I/O error occurs.
         */
        public void commit() throws IOException {
            synchronized (DiskLruCache.this) {
                if (done) {
                    throw new IllegalStateException();
                }
                if (entry.currentEditor == this) {
                    completeEdit(this, true);
                }
                done = true;
            }
        }

        /**
         * Aborts this edit. This releases the edit lock so another edit may be started on the same key.
         *
         * @throws IOException if an I/O error occurs.
         */
        public void abort() throws IOException {
            synchronized (DiskLruCache.this) {
                if (done) {
                    throw new IllegalStateException();
                }
                if (entry.currentEditor == this) {
                    completeEdit(this, false);
                }
                done = true;
            }
        }

        /**
         * Aborts this edit unless it has been committed.
         */
        public void abortUnlessCommitted() {
            synchronized (DiskLruCache.this) {
                if (!done && entry.currentEditor == this) {
                    try {
                        completeEdit(this, false);
                    } catch (IOException ignored) {
                        Logger.warn(
                                false,
                                "Http",
                                ignored,
                                "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                                "DiskLruCache",
                                true,
                                ignored.getClass().getSimpleName());
                    }
                }
            }
        }

    }

    /**
     * Mutable state for one disk LRU cache entry.
     */
    private static class State {

        /**
         * Entry key.
         */
        final String key;

        /**
         * Lengths of this entry's value files.
         */
        final long[] lengths;

        /**
         * Published value files.
         */
        final File[] cleanFiles;

        /**
         * In-progress value files.
         */
        final File[] dirtyFiles;

        /**
         * True after this entry has been published at least once.
         */
        boolean readable;

        /**
         * Active editor or null.
         */
        Editor currentEditor;

        /**
         * Sequence number of the most recent committed edit.
         */
        long sequenceNumber;

        /**
         * Creates entry state and derives clean/dirty file names.
         *
         * @param directory  cache directory
         * @param key        entry key
         * @param valueCount value count
         */
        State(final File directory, final String key, final int valueCount) {
            this.key = key;
            this.lengths = new long[valueCount];
            this.cleanFiles = new File[valueCount];
            this.dirtyFiles = new File[valueCount];

            final StringBuilder fileBuilder = new StringBuilder(key).append(Symbol.C_DOT);
            final int truncateTo = fileBuilder.length();
            for (int i = 0; i < valueCount; i++) {
                fileBuilder.append(i);
                cleanFiles[i] = new File(directory, fileBuilder.toString());
                fileBuilder.append(".tmp");
                dirtyFiles[i] = new File(directory, fileBuilder.toString());
                fileBuilder.setLength(truncateTo);
            }
        }

        /**
         * Sets lengths using decimal journal fields.
         *
         * @param strings length fields
         * @throws IOException when the journal line is invalid
         */
        void setLengths(final String[] strings) throws IOException {
            if (strings.length != lengths.length) {
                throw invalidLengths(strings);
            }

            try {
                for (int i = 0; i < strings.length; i++) {
                    lengths[i] = Long.parseLong(strings[i]);
                }
            } catch (final NumberFormatException e) {
                Logger.warn(
                        false,
                        "Http",
                        e,
                        "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                        "DiskLruCache",
                        true,
                        e.getClass().getSimpleName());
                throw invalidLengths(strings);
            }
        }

        /**
         * Appends space-prefixed lengths to a journal line.
         *
         * @param writer target writer
         * @throws IOException when writing fails
         */
        void writeLengths(final BufferSink writer) throws IOException {
            for (final long length : lengths) {
                writer.writeByte(Symbol.C_SPACE).writeDecimalLong(length);
            }
        }

        /**
         * Creates a consistent exception for malformed journal length fields.
         *
         * @param strings raw length fields
         * @return parse failure
         */
        private IOException invalidLengths(final String[] strings) {
            return new IOException("unexpected journal line: " + Arrays.toString(strings));
        }

    }

    /**
     * Journal codec for disk LRU cache records.
     */
    private static final class Journal {

        /**
         * Clean entry marker.
         */
        static final String CLEAN = "CLEAN";

        /**
         * Dirty entry marker.
         */
        static final String DIRTY = "DIRTY";

        /**
         * Removed entry marker.
         */
        static final String REMOVE = "REMOVE";

        /**
         * Read entry marker.
         */
        static final String READ = "READ";

        /**
         * Hidden constructor for the journal codec namespace.
         */
        private Journal() {
            // No initialization required.
        }

        /**
         * Reads and validates a journal header.
         *
         * @param source     journal source
         * @param appVersion expected app version
         * @param valueCount expected value count
         * @throws IOException when the header is invalid
         */
        static void readHeader(final BufferSource source, final int appVersion, final int valueCount)
                throws IOException {
            final String magic = source.readUtf8LineStrict();
            final String version = source.readUtf8LineStrict();
            final String appVersionString = source.readUtf8LineStrict();
            final String valueCountString = source.readUtf8LineStrict();
            final String blank = source.readUtf8LineStrict();
            if (!MAGIC.equals(magic) || !VERSION_1.equals(version)
                    || !Integer.toString(appVersion).equals(appVersionString)
                    || !Integer.toString(valueCount).equals(valueCountString) || !Normal.EMPTY.equals(blank)) {
                throw new IOException("unexpected journal header: [" + magic + ", " + version + ", " + valueCountString
                        + ", " + blank + "]");
            }
        }

        /**
         * Writes a journal header.
         *
         * @param writer     journal writer
         * @param appVersion app version
         * @param valueCount value count
         * @throws IOException when writing fails
         */
        static void writeHeader(final BufferSink writer, final int appVersion, final int valueCount)
                throws IOException {
            writer.writeUtf8(MAGIC).writeByte(Symbol.C_LF);
            writer.writeUtf8(VERSION_1).writeByte(Symbol.C_LF);
            writer.writeDecimalLong(appVersion).writeByte(Symbol.C_LF);
            writer.writeDecimalLong(valueCount).writeByte(Symbol.C_LF);
            writer.writeByte(Symbol.C_LF);
        }

        /**
         * Parses one journal line.
         *
         * @param line journal line
         * @return parsed journal line
         * @throws IOException when the line is invalid
         */
        static Line parseLine(final String line) throws IOException {
            final int firstSpace = line.indexOf(Symbol.C_SPACE);
            if (firstSpace == -1) {
                throw new IOException("unexpected journal line: " + line);
            }

            final int keyBegin = firstSpace + 1;
            final int secondSpace = line.indexOf(Symbol.C_SPACE, keyBegin);
            final String command = line.substring(0, firstSpace);
            final String key = secondSpace == -1 ? line.substring(keyBegin) : line.substring(keyBegin, secondSpace);
            if (REMOVE.equals(command) && secondSpace == -1) {
                return new Line(command, key, null);
            }
            if (CLEAN.equals(command) && secondSpace != -1) {
                return new Line(command, key, line.substring(secondSpace + 1).split(Symbol.SPACE));
            }
            if ((DIRTY.equals(command) || READ.equals(command)) && secondSpace == -1) {
                return new Line(command, key, null);
            }
            throw new IOException("unexpected journal line: " + line);
        }

        /**
         * Writes a dirty entry line.
         *
         * @param writer journal writer
         * @param key    entry key
         * @throws IOException when writing fails
         */
        static void writeDirty(final BufferSink writer, final String key) throws IOException {
            writer.writeUtf8(DIRTY).writeByte(Symbol.C_SPACE).writeUtf8(key).writeByte(Symbol.C_LF);
        }

        /**
         * Writes a clean entry line.
         *
         * @param writer journal writer
         * @param entry  entry state
         * @throws IOException when writing fails
         */
        static void writeClean(final BufferSink writer, final State entry) throws IOException {
            writer.writeUtf8(CLEAN).writeByte(Symbol.C_SPACE).writeUtf8(entry.key);
            entry.writeLengths(writer);
            writer.writeByte(Symbol.C_LF);
        }

        /**
         * Writes a removed entry line.
         *
         * @param writer journal writer
         * @param key    entry key
         * @throws IOException when writing fails
         */
        static void writeRemove(final BufferSink writer, final String key) throws IOException {
            writer.writeUtf8(REMOVE).writeByte(Symbol.C_SPACE).writeUtf8(key).writeByte(Symbol.C_LF);
        }

        /**
         * Writes a read entry line.
         *
         * @param writer journal writer
         * @param key    entry key
         * @throws IOException when writing fails
         */
        static void writeRead(final BufferSink writer, final String key) throws IOException {
            writer.writeUtf8(READ).writeByte(Symbol.C_SPACE).writeUtf8(key).writeByte(Symbol.C_LF);
        }

        /**
         * Parsed journal line.
         *
         * @param command command
         * @param key     entry key
         * @param lengths optional length fields
         */
        record Line(String command, String key, String[] lengths) {

            /**
             * Returns whether this line marks a clean entry.
             *
             * @return true for clean
             */
            boolean clean() {
                return CLEAN.equals(command);
            }

            /**
             * Returns whether this line marks a dirty entry.
             *
             * @return true for dirty
             */
            boolean dirty() {
                return DIRTY.equals(command);
            }

            /**
             * Returns whether this line marks a removed entry.
             *
             * @return true for removed
             */
            boolean remove() {
                return REMOVE.equals(command);
            }

            /**
             * Returns whether this line marks a read entry.
             *
             * @return true for read
             */
            boolean read() {
                return READ.equals(command);
            }

        }

    }

    /**
     * Live cache entry bound to the outer cache directory and journal lifecycle.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private class Entry extends State {

        /**
         * Creates a live entry using the outer cache value count and directory.
         *
         * @param key entry key
         */
        Entry(String key) {
            super(directory, key, valueCount);
        }

        /**
         * Returns a snapshot of this entry. This opens all streams eagerly to guarantee that we see a single published
         * snapshot. If we opened streams lazily then the streams could come from different edits.
         *
         * @return a snapshot of this entry.
         */
        Snapshot snapshot() {
            if (!Thread.holdsLock(DiskLruCache.this))
                throw new AssertionError();

            Source[] sources = new Source[valueCount];
            long[] lengths = this.lengths.clone(); // Defensive copy.
            try {
                for (int i = 0; i < valueCount; i++) {
                    sources[i] = diskFile.source(cleanFiles[i]);
                }
                return new Snapshot(key, sequenceNumber, sources, lengths);
            } catch (FileNotFoundException e) {
                Logger.warn(
                        false,
                        "Http",
                        e,
                        "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                        "DiskLruCache",
                        true,
                        e.getClass().getSimpleName());
                // A file was deleted from under us. It's a race condition. Return null.
                for (int i = 0; i < valueCount; i++) {
                    if (null != sources[i]) {
                        IoKit.close(sources[i]);
                    } else {
                        break; // We couldn't open this source, so no more sources are open.
                    }
                }
                // Since the entry is no longer valid, remove it from the cache.
                try {
                    removeEntry(this);
                } catch (IOException ignored) {
                    Logger.warn(
                            false,
                            "Http",
                            ignored,
                            "HTTP cache operation failed: provider={}, recoverable={}, exception={}",
                            "DiskLruCache",
                            true,
                            ignored.getClass().getSimpleName());
                }
                return null;
            }
        }

    }

}
