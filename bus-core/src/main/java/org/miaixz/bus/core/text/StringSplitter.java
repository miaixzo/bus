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
package org.miaixz.bus.core.text;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.miaixz.bus.core.center.iterator.ComputeIterator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.text.finder.TextFinder;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * String splitting iterator.
 * <p>
 * This iterator implements a lazy splitting mode for strings. It does not perform the splitting immediately upon
 * instantiation, but only when {@link #hasNext()} is called or during iteration. This approach is memory efficient for
 * large strings.
 * </p>
 * <p>
 * Note: This iterator is not thread-safe.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringSplitter extends ComputeIterator<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852233699808L;

    /**
     * The text to be split.
     */
    private final String text;

    /**
     * The text finder used to locate separators.
     */
    private final TextFinder finder;

    /**
     * The maximum number of parts to return.
     */
    private final int limit;

    /**
     * Whether to ignore empty strings resulting from splitting.
     */
    private final boolean ignoreEmpty;

    /**
     * The start index for the next split operation (the end position of the previous split).
     */
    private int offset;

    /**
     * Counter for the number of split parts found so far, used to check against the limit.
     */
    private int count;

    /**
     * Constructs a new {@code StringSplitter} instance.
     *
     * @param text            The text to be split, cannot be {@code null}.
     * @param separatorFinder The separator matcher used to find split boundaries.
     * @param limit           The maximum number of parts to return. A value less than or equal to 0 means no limit.
     * @param ignoreEmpty     Whether to ignore empty strings (e.g., resulting from adjacent separators) in the output.
     */
    public StringSplitter(final CharSequence text, final TextFinder separatorFinder, final int limit,
            final boolean ignoreEmpty) {
        Assert.notNull(text, "Text must be not null!");
        this.text = text.toString();
        this.finder = separatorFinder.setText(text);
        this.limit = limit > 0 ? limit : Integer.MAX_VALUE;
        this.ignoreEmpty = ignoreEmpty;
    }

    /**
     * Computes the next element in the split sequence.
     *
     * @return The next split string, or {@code null} if no more parts are available.
     */
    @Override
    protected String computeNext() {
        // Reached the limit or the end of the string, finish.
        if (count >= limit || offset > text.length()) {
            return null;
        }

        // Reached the splitting limit (limit - 1 because the last part is the remainder).
        if (count == (limit - 1)) {
            // If at the end of the string and ignoring empty, return null.
            if (ignoreEmpty && offset == text.length()) {
                return null;
            }

            // Return the remaining part of the string as the last element.
            count++;
            return text.substring(offset);
        }

        String result;
        int start;
        do {
            start = finder.start(offset);
            // No separator found, finish.
            if (start < 0) {
                // If no more separators, but characters remain, return the rest as a single segment.
                if (offset <= text.length()) {
                    result = text.substring(offset);
                    if (!ignoreEmpty || !result.isEmpty()) {
                        // Return non-empty string or empty string if ignoreEmpty is false.
                        offset = Integer.MAX_VALUE;
                        return result;
                    }
                }
                return null;
            }

            // Found a new separator position.
            result = text.substring(offset, start);
            offset = finder.end(start);
        } while (ignoreEmpty && result.isEmpty()); // Continue searching if the result is empty and we are ignoring
                                                   // empty strings.

        count++;
        return result;
    }

    /**
     * Resets the splitter to its initial state, allowing re-iteration from the beginning.
     */
    @Override
    public void reset() {
        super.reset();
        this.finder.reset();
        this.offset = 0;
        this.count = 0;
    }

    /**
     * Retrieves the split parts as an array of strings.
     *
     * @param trim Whether to trim whitespace from each element in the resulting array.
     * @return An array containing the split strings.
     */
    public String[] toArray(final boolean trim) {
        return toList(trim).toArray(new String[0]);
    }

    /**
     * Retrieves the split parts as a list of strings.
     *
     * @param trim Whether to trim whitespace from each element in the resulting list.
     * @return A list containing the split strings.
     */
    public List<String> toList(final boolean trim) {
        return toList(trim ? StringKit::trim : Function.identity());
    }

    /**
     * Retrieves the split parts as a list of objects, applying a mapping function to each string part.
     *
     * @param <T>     The type of elements in the resulting list.
     * @param mapping The function to map each split string to an object of type {@code T}.
     * @return A list of mapped objects.
     */
    public <T> List<T> toList(final Function<String, T> mapping) {
        final List<T> result = new ArrayList<>();
        while (this.hasNext()) {
            final T apply = mapping.apply(this.next());
            if (ignoreEmpty && ObjectKit.isEmptyIfString(apply)) {
                // If the mapped object is still a String and empty, and empty strings should be ignored, skip it.
                continue;
            }
            result.add(apply);
        }
        if (result.isEmpty()) {
            return new ArrayList<>(0);
        }
        return result;
    }

}
