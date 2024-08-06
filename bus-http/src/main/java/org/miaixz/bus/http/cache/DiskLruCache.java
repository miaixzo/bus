/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.cache;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.FaultHideSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.logger.Logger;

/**
 * 使用文件系统上有限空间的缓存。每个缓存条目都有一个字符串键和固定数量的值 每个键必须匹配regex [a-z0-9_-]{1,64}。值是字节序列，可以作为流或文件访问
 * 每个值必须在{@code 0}和{@code Integer之间。MAX_VALUE}字节的长度
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DiskLruCache implements Closeable, Flushable {

    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_TEMP = "journal.tmp";
    static final String JOURNAL_FILE_BACKUP = "journal.bkp";
    static final String MAGIC = "libcore.io.DiskLruCache";
    static final String VERSION_1 = Symbol.ONE;
    static final long ANY_SEQUENCE_NUMBER = -1;
    static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,120}");
    private static final String CLEAN = "CLEAN";
    private static final String DIRTY = "DIRTY";
    private static final String REMOVE = "REMOVE";
    private static final String READ = "READ";
    final DiskFile diskFile;
    /**
     * 缓存存储其数据的目录
     */
    final File directory;
    final int valueCount;
    final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap<>(0, Normal.DEFAULT_LOAD_FACTOR, true);
    private final File journalFile;
    private final File journalFileTmp;
    private final File journalFileBackup;
    private final int appVersion;
    private final Executor executor;
    BufferSink journalWriter;
    int redundantOpCount;
    boolean hasJournalErrors;
    boolean initialized;
    /**
     * 如果缓存已关闭，则为true
     */
    boolean closed;
    boolean mostRecentTrimFailed;
    boolean mostRecentRebuildFailed;
    /**
     * 存用于存储其数据的最大字节数
     */
    private long maxSize;
    /**
     * 当前用于在此缓存中存储值的字节数
     */
    private long size = 0;
    /**
     * 为了区分旧快照和当前快照，每次提交编辑时都会给每个条目一个序列号。 如果快照的序列号不等于其条目的序列号，则该快照将失效
     */
    private long nextSequenceNumber = 0;

    DiskLruCache(DiskFile diskFile, File directory, int appVersion, int valueCount, long maxSize, Executor executor) {
        this.diskFile = diskFile;
        this.directory = directory;
        this.appVersion = appVersion;
        this.journalFile = new File(directory, JOURNAL_FILE);
        this.journalFileTmp = new File(directory, JOURNAL_FILE_TEMP);
        this.journalFileBackup = new File(directory, JOURNAL_FILE_BACKUP);
        this.valueCount = valueCount;
        this.maxSize = maxSize;
        this.executor = executor;
    }

    /**
     * 创建一个驻留在{@code directory}中的缓存。此缓存在第一次访问时惰性初始化，如果它不存在，将创建它.
     *
     * @param diskFile   文件系统
     * @param directory  一个可写目录
     * @param appVersion 版本信息
     * @param valueCount 每个缓存条目的值数目.
     * @param maxSize    此缓存应用于存储的最大字节数
     * @return the disk cache
     */
    public static DiskLruCache create(DiskFile diskFile, File directory, int appVersion, int valueCount, long maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        if (valueCount <= 0) {
            throw new IllegalArgumentException("valueCount <= 0");
        }

        Executor executor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                Builder.threadFactory("Httpd DiskLruCache", true));

        return new DiskLruCache(diskFile, directory, appVersion, valueCount, maxSize, executor);
    }

    public synchronized void initialize() throws IOException {
        assert Thread.holdsLock(this);

        if (initialized) {
            return;
        }

        // 如果存在bkp文件，就使用它
        if (diskFile.exists(journalFileBackup)) {
            // 如果日志文件也存在，删除备份文件
            if (diskFile.exists(journalFile)) {
                diskFile.delete(journalFileBackup);
            } else {
                diskFile.rename(journalFileBackup, journalFile);
            }
        }

        if (diskFile.exists(journalFile)) {
            try {
                readJournal();
                processJournal();
                initialized = true;
                return;
            } catch (IOException journalIsCorrupt) {
                Logger.warn(
                        "DiskLruCache " + directory + " is corrupt: " + journalIsCorrupt.getMessage() + ", removing",
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

    private void readJournal() throws IOException {
        try (BufferSource source = IoKit.buffer(diskFile.source(journalFile))) {
            String magic = source.readUtf8LineStrict();
            String version = source.readUtf8LineStrict();
            String appVersionString = source.readUtf8LineStrict();
            String valueCountString = source.readUtf8LineStrict();
            String blank = source.readUtf8LineStrict();
            if (!MAGIC.equals(magic) || !VERSION_1.equals(version)
                    || !Integer.toString(appVersion).equals(appVersionString)
                    || !Integer.toString(valueCount).equals(valueCountString) || !Normal.EMPTY.equals(blank)) {
                throw new IOException("unexpected journal header: [" + magic + ", " + version + ", " + valueCountString
                        + ", " + blank + "]");
            }

            int lineCount = 0;
            while (true) {
                try {
                    readJournalLine(source.readUtf8LineStrict());
                    lineCount++;
                } catch (EOFException endOfJournal) {
                    break;
                }
            }
            redundantOpCount = lineCount - lruEntries.size();

            // 如果我们以截断的行结束，则在添加日志之前重新生成它
            if (!source.exhausted()) {
                rebuildJournal();
            } else {
                journalWriter = newJournalWriter();
            }
        }
    }

    private void readJournalLine(String line) throws IOException {
        int firstSpace = line.indexOf(Symbol.C_SPACE);
        if (firstSpace == -1) {
            throw new IOException("unexpected journal line: " + line);
        }

        int keyBegin = firstSpace + 1;
        int secondSpace = line.indexOf(Symbol.C_SPACE, keyBegin);
        final String key;
        if (secondSpace == -1) {
            key = line.substring(keyBegin);
            if (firstSpace == REMOVE.length() && line.startsWith(REMOVE)) {
                lruEntries.remove(key);
                return;
            }
        } else {
            key = line.substring(keyBegin, secondSpace);
        }

        Entry entry = lruEntries.get(key);
        if (null == entry) {
            entry = new Entry(key);
            lruEntries.put(key, entry);
        }

        if (secondSpace != -1 && firstSpace == CLEAN.length() && line.startsWith(CLEAN)) {
            String[] parts = line.substring(secondSpace + 1).split(Symbol.SPACE);
            entry.readable = true;
            entry.currentEditor = null;
            entry.setLengths(parts);
        } else if (secondSpace == -1 && firstSpace == DIRTY.length() && line.startsWith(DIRTY)) {
            entry.currentEditor = new Editor(entry);
        } else if (secondSpace == -1 && firstSpace == READ.length() && line.startsWith(READ)) {
            // This work was already done by calling lruEntries.get().
        } else {
            throw new IOException("unexpected journal line: " + line);
        }
    }

    private BufferSink newJournalWriter() throws FileNotFoundException {
        Sink fileSink = diskFile.appendingSink(journalFile);
        Sink faultHidingSink = new FaultHideSink(fileSink) {
            @Override
            protected void onException(IOException e) {
                assert (Thread.holdsLock(DiskLruCache.this));
                hasJournalErrors = true;
            }
        };
        return IoKit.buffer(faultHidingSink);
    }

    /**
     * 计算初始大小并收集垃圾作为打开缓存的一部分。脏条目被认为是不一致的，将被删除
     *
     * @throws IOException 异常
     */
    private void processJournal() throws IOException {
        diskFile.delete(journalFileTmp);
        for (Iterator<Entry> i = lruEntries.values().iterator(); i.hasNext();) {
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
     * 创建一个删除冗余信息的新日志。如果存在当前日志，它将替换它
     *
     * @throws IOException 异常
     */
    synchronized void rebuildJournal() throws IOException {
        if (null != journalWriter) {
            journalWriter.close();
        }

        try (BufferSink writer = IoKit.buffer(IoKit.sink(journalFileTmp))) {
            writer.writeUtf8(MAGIC).writeByte(Symbol.C_LF);
            writer.writeUtf8(VERSION_1).writeByte(Symbol.C_LF);
            writer.writeDecimalLong(appVersion).writeByte(Symbol.C_LF);
            writer.writeDecimalLong(valueCount).writeByte(Symbol.C_LF);
            writer.writeByte(Symbol.C_LF);

            for (Entry entry : lruEntries.values()) {
                if (null != entry.currentEditor) {
                    writer.writeUtf8(DIRTY).writeByte(Symbol.C_SPACE);
                    writer.writeUtf8(entry.key);
                    writer.writeByte(Symbol.C_LF);
                } else {
                    writer.writeUtf8(CLEAN).writeByte(Symbol.C_SPACE);
                    writer.writeUtf8(entry.key);
                    entry.writeLengths(writer);
                    writer.writeByte(Symbol.C_LF);
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
     * 返回名为{@code key}的条目的快照，如果条目不存在，则返回null， 否则当前无法读取。如果返回一个值，它将被移动到LRU队列的头部
     *
     * @param key 缓存key
     * @return the 快照信息
     * @throws IOException 异常
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
        journalWriter.writeUtf8(READ).writeByte(Symbol.C_SPACE).writeUtf8(key).writeByte(Symbol.C_LF);
        if (journalRebuildRequired()) {
            executor.execute(cleanupRunnable);
        }

        return snapshot;
    }

    /**
     * 返回名为{@code key}的条目的编辑器，如果另一个编辑正在进行，则返回null
     *
     * @param key 文件key
     * @return 编辑器
     * @throws IOException 异常
     */
    public Editor edit(String key) throws IOException {
        return edit(key, ANY_SEQUENCE_NUMBER);
    }

    synchronized Editor edit(String key, long expectedSequenceNumber) throws IOException {
        initialize();

        checkNotClosed();
        validateKey(key);
        Entry entry = lruEntries.get(key);
        if (expectedSequenceNumber != ANY_SEQUENCE_NUMBER
                && (null == entry || entry.sequenceNumber != expectedSequenceNumber)) {
            return null;
        }
        if (null != entry && null != entry.currentEditor) {
            return null;
        }
        if (mostRecentTrimFailed || mostRecentRebuildFailed) {
            executor.execute(cleanupRunnable);
            return null;
        }

        // 在创建文件之前刷新日志，以防止文件泄漏
        journalWriter.writeUtf8(DIRTY).writeByte(Symbol.C_SPACE).writeUtf8(key).writeByte(Symbol.C_LF);
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
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Returns the maximum number of bytes that this cache should use to store its data.
     */
    public synchronized long getMaxSize() {
        return maxSize;
    }

    /**
     * Changes the maximum number of bytes the cache can store and queues a job to trim the existing store, if
     * necessary.
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
     */
    public synchronized long size() throws IOException {
        initialize();
        return size;
    }

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
        if (entry.readable | success) {
            entry.readable = true;
            journalWriter.writeUtf8(CLEAN).writeByte(Symbol.C_SPACE);
            journalWriter.writeUtf8(entry.key);
            entry.writeLengths(journalWriter);
            journalWriter.writeByte('\n');
            if (success) {
                entry.sequenceNumber = nextSequenceNumber++;
            }
        } else {
            lruEntries.remove(entry.key);
            journalWriter.writeUtf8(REMOVE).writeByte(Symbol.C_SPACE);
            journalWriter.writeUtf8(entry.key);
            journalWriter.writeByte('\n');
        }
        journalWriter.flush();

        if (size > maxSize || journalRebuildRequired()) {
            executor.execute(cleanupRunnable);
        }
    }

    /**
     * We only rebuild the journal when it will halve the size of the journal and eliminate at least 2000 ops.
     */
    boolean journalRebuildRequired() {
        final int redundantOpCompactThreshold = 2000;
        return redundantOpCount >= redundantOpCompactThreshold && redundantOpCount >= lruEntries.size();
    }

    /**
     * Drops the entry for {@code key} if it exists and can be removed. If the entry for {@code key} is currently being
     * edited, that edit will complete normally but its value will not be stored.
     *
     * @return true if an entry was removed.
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
        journalWriter.writeUtf8(REMOVE).writeByte(Symbol.C_SPACE).writeUtf8(entry.key).writeByte('\n');
        lruEntries.remove(entry.key);

        if (journalRebuildRequired()) {
            executor.execute(cleanupRunnable);
        }

        return true;
    }

    /**
     * Returns true if this cache has been closed.
     */
    public synchronized boolean isClosed() {
        return closed;
    }

    private synchronized void checkNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException("cache is closed");
        }
    }

    /**
     * Force buffered operations to the filesystem.
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
     */
    @Override
    public synchronized void close() throws IOException {
        if (!initialized || closed) {
            closed = true;
            return;
        }
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
     */
    public void delete() throws IOException {
        close();
        diskFile.deleteContents(directory);
    }

    /**
     * Deletes all stored values from the cache. In-flight edits will complete normally but their values will not be
     * stored.
     */
    public synchronized void evictAll() throws IOException {
        initialize();
        // Copying for safe iteration.
        for (Entry entry : lruEntries.values().toArray(new Entry[lruEntries.size()])) {
            removeEntry(entry);
        }
        mostRecentTrimFailed = false;
    }

    private void validateKey(String key) {
        Matcher matcher = LEGAL_KEY_PATTERN.matcher(key);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,120}: \"" + key + "\"");
        }
    }

    /**
     * 返回缓存当前项的迭代器。这个迭代器不会抛出{@code ConcurrentModificationException}，
     * 调用者必须{@link Snapshot#close}每个由{@link Iterator#next}返回的快照 如果做不到这一点，就会泄漏打开的文件,返回的迭代器支持 {@link Iterator#remove}.
     *
     * @return 返回迭代器
     * @throws IOException 异常
     */
    public synchronized Iterator<Snapshot> snapshots() throws IOException {
        initialize();
        return new Iterator<>() {
            /**
             * 迭代条目的副本以防止并发修改错误
             */
            final Iterator<Entry> delegate = new ArrayList<>(lruEntries.values()).iterator();

            /**
             * 要从{@link #next}返回的快照。如果还没有计算出来，就是Null
             */
            Snapshot nextSnapshot;

            /**
             * 要使用{@link #remove}删除的快照。如果删除是非法的，则为Null
             */
            Snapshot removeSnapshot;

            @Override
            public boolean hasNext() {
                if (nextSnapshot != null)
                    return true;

                synchronized (DiskLruCache.this) {
                    // 如果缓存关闭，则截断迭代器
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

            @Override
            public Snapshot next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                removeSnapshot = nextSnapshot;
                nextSnapshot = null;
                return removeSnapshot;
            }

            @Override
            public void remove() {
                if (removeSnapshot == null)
                    throw new IllegalStateException("remove() before next()");
                try {
                    DiskLruCache.this.remove(removeSnapshot.key);
                } catch (IOException ignored) {
                    // 这里没什么用。未能从缓存中删除。这很可能是因为无法更新日志，但是缓存的条目仍然没有了
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
     *
     * <p>
     * All operations on a file system are racy. For example, guarding a call to {@link #source} with {@link #exists}
     * does not guarantee that {@link FileNotFoundException} will not be thrown. The file may be moved between the two
     * calls!
     *
     * <p>
     * This interface is less ambitious than {@link java.nio.file.FileSystem} introduced in Java 7. It lacks important
     * features like file watching, metadata, permissions, and disk space information. In exchange for these
     * limitations, this interface is easier to implement and works on all versions of Java and Android.
     */
    public static interface DiskFile {

        /**
         * The host machine's local file system.
         */
        DiskFile SYSTEM = new DiskFile() {
            @Override
            public Source source(File file) throws FileNotFoundException {
                return IoKit.source(file);
            }

            @Override
            public Sink sink(File file) throws FileNotFoundException {
                try {
                    return IoKit.sink(file);
                } catch (FileNotFoundException e) {
                    // Maybe the parent directory doesn't exist? Try creating it first.
                    file.getParentFile().mkdirs();
                    return IoKit.sink(file);
                }
            }

            @Override
            public Sink appendingSink(File file) throws FileNotFoundException {
                try {
                    return IoKit.appendingSink(file);
                } catch (FileNotFoundException e) {
                    // Maybe the parent directory doesn't exist? Try creating it first.
                    file.getParentFile().mkdirs();
                    return IoKit.appendingSink(file);
                }
            }

            @Override
            public void delete(File file) throws IOException {
                // If delete() fails, make sure it's because the file didn't exist!
                if (!file.delete() && file.exists()) {
                    throw new IOException("failed to delete " + file);
                }
            }

            @Override
            public boolean exists(File file) {
                return file.exists();
            }

            @Override
            public long size(File file) {
                return file.length();
            }

            @Override
            public void rename(File from, File to) throws IOException {
                delete(to);
                if (!from.renameTo(to)) {
                    throw new IOException("failed to rename " + from + " to " + to);
                }
            }

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
        };

        /**
         * Reads from {@code file}.
         */
        Source source(File file) throws FileNotFoundException;

        /**
         * Writes to {@code file}, discarding any data already present. Creates parent directories if necessary.
         */
        Sink sink(File file) throws FileNotFoundException;

        /**
         * Writes to {@code file}, appending if data is already present. Creates parent directories if necessary.
         */
        Sink appendingSink(File file) throws FileNotFoundException;

        /**
         * Deletes {@code file} if it exists. Throws if the file exists and cannot be deleted.
         */
        void delete(File file) throws IOException;

        /**
         * Returns true if {@code file} exists on the file system.
         */
        boolean exists(File file);

        /**
         * Returns the number of bytes stored in {@code file}, or 0 if it does not exist.
         */
        long size(File file);

        /**
         * Renames {@code from} to {@code to}. Throws if the file cannot be renamed.
         */
        void rename(File from, File to) throws IOException;

        /**
         * Recursively delete the contents of {@code directory}. Throws an IOException if any file could not be deleted,
         * or if {@code dir} is not a readable directory.
         */
        void deleteContents(File directory) throws IOException;

    }

    /**
     * 快照信息
     */
    public final class Snapshot implements Closeable {

        private final String key;
        private final long sequenceNumber;
        private final Source[] sources;
        private final long[] lengths;

        Snapshot(String key, long sequenceNumber, Source[] sources, long[] lengths) {
            this.key = key;
            this.sequenceNumber = sequenceNumber;
            this.sources = sources;
            this.lengths = lengths;
        }

        public String key() {
            return key;
        }

        /**
         * Returns an editor for this snapshot's entry, or null if either the entry has changed since this snapshot was
         * created or if another edit is in progress.
         */
        public Editor edit() throws IOException {
            return DiskLruCache.this.edit(key, sequenceNumber);
        }

        /**
         * Returns the unbuffered stream with the value for {@code index}.
         */
        public Source getSource(int index) {
            return sources[index];
        }

        /**
         * Returns the byte length of the value for {@code index}.
         */
        public long getLength(int index) {
            return lengths[index];
        }

        public void close() {
            for (Source in : sources) {
                IoKit.close(in);
            }
        }
    }

    /**
     * Edits the values for an entry.
     */
    public final class Editor {

        final Entry entry;
        final boolean[] written;
        private boolean done;

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
                        // This file is potentially leaked. Not much we can do about that.
                    }
                }
                entry.currentEditor = null;
            }
        }

        /**
         * Returns an unbuffered input stream to read the last committed value, or null if no value has been committed.
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
                    return null;
                }
            }
        }

        /**
         * Returns a new unbuffered output stream to write the value at {@code index}. If the underlying output stream
         * encounters errors when writing to the filesystem, this edit will be aborted when {@link #commit} is called.
         * The returned output stream does not throw IOExceptions.
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
                    return IoKit.blackhole();
                }
                return new FaultHideSink(sink) {
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
         * 中止这个编辑。这释放了编辑锁，因此可以在同一个键上启动另一个编辑
         *
         * @throws IOException 异常
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

        public void abortUnlessCommitted() {
            synchronized (DiskLruCache.this) {
                if (!done && entry.currentEditor == this) {
                    try {
                        completeEdit(this, false);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private class Entry {

        final String key;

        /**
         * Lengths of this entry's files.
         */
        final long[] lengths;
        final File[] cleanFiles;
        final File[] dirtyFiles;

        /**
         * True if this entry has ever been published.
         */
        boolean readable;

        /**
         * The ongoing edit or null if this entry is not being edited.
         */
        Editor currentEditor;

        /**
         * The sequence number of the most recently committed edit to this entry.
         */
        long sequenceNumber;

        Entry(String key) {
            this.key = key;

            lengths = new long[valueCount];
            cleanFiles = new File[valueCount];
            dirtyFiles = new File[valueCount];

            StringBuilder fileBuilder = new StringBuilder(key).append(Symbol.C_DOT);
            int truncateTo = fileBuilder.length();
            for (int i = 0; i < valueCount; i++) {
                fileBuilder.append(i);
                cleanFiles[i] = new File(directory, fileBuilder.toString());
                fileBuilder.append(".tmp");
                dirtyFiles[i] = new File(directory, fileBuilder.toString());
                fileBuilder.setLength(truncateTo);
            }
        }

        /**
         * Set lengths using decimal numbers like "10123".
         */
        void setLengths(String[] strings) throws IOException {
            if (strings.length != valueCount) {
                throw invalidLengths(strings);
            }

            try {
                for (int i = 0; i < strings.length; i++) {
                    lengths[i] = Long.parseLong(strings[i]);
                }
            } catch (NumberFormatException e) {
                throw invalidLengths(strings);
            }
        }

        /**
         * Append space-prefixed lengths to {@code writer}.
         */
        void writeLengths(BufferSink writer) throws IOException {
            for (long length : lengths) {
                writer.writeByte(Symbol.C_SPACE).writeDecimalLong(length);
            }
        }

        private IOException invalidLengths(String[] strings) throws IOException {
            throw new IOException("unexpected journal line: " + Arrays.toString(strings));
        }

        /**
         * Returns a snapshot of this entry. This opens all streams eagerly to guarantee that we see a single published
         * snapshot. If we opened streams lazily then the streams could come from different edits.
         */
        Snapshot snapshot() {
            if (!Thread.holdsLock(DiskLruCache.this))
                throw new AssertionError();

            Source[] sources = new Source[valueCount];
            long[] lengths = this.lengths.clone();
            try {
                for (int i = 0; i < valueCount; i++) {
                    sources[i] = diskFile.source(cleanFiles[i]);
                }
                return new Snapshot(key, sequenceNumber, sources, lengths);
            } catch (FileNotFoundException e) {
                for (int i = 0; i < valueCount; i++) {
                    if (null != sources[i]) {
                        IoKit.close(sources[i]);
                    } else {
                        break;
                    }
                }
                try {
                    removeEntry(this);
                } catch (IOException ignored) {
                }
                return null;
            }
        }
    }

    private final Runnable cleanupRunnable = new Runnable() {
        public void run() {
            synchronized (DiskLruCache.this) {
                if (!initialized | closed) {
                    return;
                }

                try {
                    trimToSize();
                } catch (IOException ignored) {
                    mostRecentTrimFailed = true;
                }

                try {
                    if (journalRebuildRequired()) {
                        rebuildJournal();
                        redundantOpCount = 0;
                    }
                } catch (IOException e) {
                    mostRecentRebuildFailed = true;
                    journalWriter = IoKit.buffer(IoKit.blackhole());
                }
            }
        }
    };

}
