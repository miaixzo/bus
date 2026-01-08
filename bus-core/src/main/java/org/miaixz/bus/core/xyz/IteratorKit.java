/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.xyz;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.center.iterator.*;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.text.StringJoiner;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class for {@link Iterable} and {@link Iterator}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IteratorKit extends IteratorValidator {

    /**
     * Gets an {@link Iterator} from an {@link Iterable}.
     *
     * @param <T>      the element type
     * @param iterable the {@link Iterable} to get the iterator from
     * @return the iterator, or {@code null} if the iterable is {@code null}
     */
    public static <T> Iterator<T> getIter(final Iterable<T> iterable) {
        return null == iterable ? null : iterable.iterator();
    }

    /**
     * Creates a map from an iterator of objects, where the keys are extracted from a specified field of the objects.
     * This is useful for looking up objects by a unique ID. For example: license plate number = car.
     *
     * @param <K>       the type of the key
     * @param <V>       the type of the value (object)
     * @param iter      the iterator of objects
     * @param fieldName the name of the field to be used as the key (its value will be obtained via reflection)
     * @return a map where keys are field values and values are the corresponding objects
     */
    public static <K, V> Map<K, V> fieldValueMap(final Iterator<V> iter, final String fieldName) {
        return MapKit.putAll(new HashMap<>(), iter, (value) -> (K) FieldKit.getFieldValue(value, fieldName));
    }

    /**
     * Creates a new map from two fields of the objects in an iterator.
     *
     * @param <K>               the type of the key
     * @param <V>               the type of the value
     * @param iter              the iterator of objects
     * @param fieldNameForKey   the name of the field to be used as the key (obtained via reflection)
     * @param fieldNameForValue the name of the field to be used as the value (obtained via reflection)
     * @return a map constructed from the specified fields
     */
    public static <K, V> Map<K, V> fieldValueAsMap(
            final Iterator<?> iter,
            final String fieldNameForKey,
            final String fieldNameForValue) {
        return MapKit.putAll(
                new HashMap<>(),
                iter,
                (value) -> (K) FieldKit.getFieldValue(value, fieldNameForKey),
                (value) -> (V) FieldKit.getFieldValue(value, fieldNameForValue));
    }

    /**
     * Extracts a specific field from each bean in an iterable and returns a new list of these field values.
     *
     * @param <V>       the type of the objects in the iterable
     * @param <R>       the type of the field value
     * @param iterable  the iterable of objects
     * @param fieldName the name of the field to extract (obtained via reflection)
     * @return a list of the extracted field values
     */
    public static <V, R> List<R> fieldValueList(final Iterable<V> iterable, final String fieldName) {
        return fieldValueList(getIter(iterable), fieldName);
    }

    /**
     * Extracts a specific field from each bean in an iterator and returns a new list of these field values.
     *
     * @param <V>       the type of the objects in the iterator
     * @param <R>       the type of the field value
     * @param iter      the iterator of objects
     * @param fieldName the name of the field to extract (obtained via reflection)
     * @return a list of the extracted field values
     */
    public static <V, R> List<R> fieldValueList(final Iterator<V> iter, final String fieldName) {
        final List<R> result = new ArrayList<>();
        if (null != iter) {
            V value;
            while (iter.hasNext()) {
                value = iter.next();
                result.add((R) FieldKit.getFieldValue(value, fieldName));
            }
        }
        return result;
    }

    /**
     * Joins the elements of an iterator into a string using the specified conjunction. If an element is an array,
     * {@link Iterable}, or {@link Iterator}, it is recursively joined.
     *
     * @param <T>         the element type
     * @param iterator    the iterator
     * @param conjunction the delimiter to use
     * @return the joined string
     */
    public static <T> String join(final Iterator<T> iterator, final CharSequence conjunction) {
        return StringJoiner.of(conjunction).append(iterator).toString();
    }

    /**
     * Joins the elements of an iterator into a string using the specified conjunction, with a prefix and suffix for
     * each element. If an element is an array, {@link Iterable}, or {@link Iterator}, it is recursively joined.
     *
     * @param <T>         the element type
     * @param iterator    the iterator
     * @param conjunction the delimiter to use
     * @param prefix      the prefix to add to each element ({@code null} for no prefix)
     * @param suffix      the suffix to add to each element ({@code null} for no suffix)
     * @return the joined string
     */
    public static <T> String join(
            final Iterator<T> iterator,
            final CharSequence conjunction,
            final String prefix,
            final String suffix) {
        return StringJoiner.of(conjunction, prefix, suffix).setWrapElement(true).append(iterator).toString();
    }

    /**
     * Joins the elements of an iterator into a string using the specified conjunction and a function to convert
     * elements to strings. If an element is an array, {@link Iterable}, or {@link Iterator}, it is recursively joined.
     *
     * @param <T>         the element type
     * @param iterator    the iterator
     * @param conjunction the delimiter to use
     * @param func        the function to convert each element to a string
     * @return the joined string
     */
    public static <T> String join(
            final Iterator<T> iterator,
            final CharSequence conjunction,
            final Function<T, ? extends CharSequence> func) {
        if (null == iterator) {
            return null;
        }

        return StringJoiner.of(conjunction).append(iterator, func).toString();
    }

    /**
     * Converts lists of keys and values into a Map. The keys are the primary reference. If there are more keys than
     * values, the excess keys will have a {@code null} value. If there are more values than keys, the excess values are
     * ignored.
     *
     * @param <K>    the key type
     * @param <V>    the value type
     * @param keys   the iterable of keys
     * @param values the iterable of values
     * @return the resulting map
     */
    public static <K, V> Map<K, V> toMap(final Iterable<K> keys, final Iterable<V> values) {
        return toMap(keys, values, false);
    }

    /**
     * Converts lists of keys and values into a Map. The keys are the primary reference. If there are more keys than
     * values, the excess keys will have a {@code null} value. If there are more values than keys, the excess values are
     * ignored.
     *
     * @param <K>     the key type
     * @param <V>     the value type
     * @param keys    the iterable of keys
     * @param values  the iterable of values
     * @param isOrder whether to create an ordered map (e.g., {@link java.util.LinkedHashMap})
     * @return the resulting map
     */
    public static <K, V> Map<K, V> toMap(final Iterable<K> keys, final Iterable<V> values, final boolean isOrder) {
        return toMap(null == keys ? null : keys.iterator(), null == values ? null : values.iterator(), isOrder);
    }

    /**
     * Converts iterators of keys and values into a Map. The keys are the primary reference. If there are more keys than
     * values, the excess keys will have a {@code null} value. If there are more values than keys, the excess values are
     * ignored.
     *
     * @param <K>    the key type
     * @param <V>    the value type
     * @param keys   the iterator of keys
     * @param values the iterator of values
     * @return the resulting map
     */
    public static <K, V> Map<K, V> toMap(final Iterator<K> keys, final Iterator<V> values) {
        return toMap(keys, values, false);
    }

    /**
     * Converts iterators of keys and values into a Map. The keys are the primary reference. If there are more keys than
     * values, the excess keys will have a {@code null} value. If there are more values than keys, the excess values are
     * ignored.
     *
     * @param <K>     the key type
     * @param <V>     the value type
     * @param keys    the iterator of keys
     * @param values  the iterator of values
     * @param isOrder whether to create an ordered map (e.g., {@link java.util.LinkedHashMap})
     * @return the resulting map
     */
    public static <K, V> Map<K, V> toMap(final Iterator<K> keys, final Iterator<V> values, final boolean isOrder) {
        final Map<K, V> resultMap = MapKit.newHashMap(isOrder);
        if (isNotEmpty(keys)) {
            while (keys.hasNext()) {
                resultMap.put(keys.next(), (null != values && values.hasNext()) ? values.next() : null);
            }
        }
        return resultMap;
    }

    /**
     * Converts an iterable to a {@link HashMap} where values are lists.
     *
     * @param <K>       the key type
     * @param <V>       the value type
     * @param iterable  the iterable of values
     * @param keyMapper the function to map a value to a key
     * @return a {@link HashMap}
     */
    public static <K, V> Map<K, List<V>> toListMap(final Iterable<V> iterable, final Function<V, K> keyMapper) {
        return toListMap(iterable, keyMapper, v -> v);
    }

    /**
     * Converts an iterable to a {@link HashMap} where values are lists.
     *
     * @param <T>         the type of the elements in the iterable
     * @param <K>         the key type
     * @param <V>         the value type in the list
     * @param iterable    the iterable of values
     * @param keyMapper   the function to map an element to a key
     * @param valueMapper the function to map an element to a value in the list
     * @return a {@link HashMap}
     */
    public static <T, K, V> Map<K, List<V>> toListMap(
            final Iterable<T> iterable,
            final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        return toListMap(MapKit.newHashMap(), iterable, keyMapper, valueMapper);
    }

    /**
     * Converts an iterable to a map where values are lists.
     *
     * @param <T>         the type of the elements in the iterable
     * @param <K>         the key type
     * @param <V>         the value type in the list
     * @param resultMap   the result map to populate, allowing for custom map types
     * @param iterable    the iterable of values
     * @param keyMapper   the function to map an element to a key
     * @param valueMapper the function to map an element to a value in the list
     * @return the populated map
     */
    public static <T, K, V> Map<K, List<V>> toListMap(
            Map<K, List<V>> resultMap,
            final Iterable<T> iterable,
            final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        if (null == resultMap) {
            resultMap = MapKit.newHashMap();
        }
        if (ObjectKit.isNull(iterable)) {
            return resultMap;
        }

        for (final T value : iterable) {
            resultMap.computeIfAbsent(keyMapper.apply(value), k -> new ArrayList<>()).add(valueMapper.apply(value));
        }

        return resultMap;
    }

    /**
     * Converts an iterable to a {@link HashMap}.
     *
     * @param <K>       the key type
     * @param <V>       the value type
     * @param iterable  the iterable of values
     * @param keyMapper the function to map a value to a key
     * @return a {@link HashMap}
     */
    public static <K, V> Map<K, V> toMap(final Iterable<V> iterable, final Function<V, K> keyMapper) {
        return toMap(iterable, keyMapper, Function.identity());
    }

    /**
     * Converts an iterable to a {@link HashMap}.
     *
     * @param <T>         the type of the elements in the iterable
     * @param <K>         the key type
     * @param <V>         the value type
     * @param iterable    the iterable of values
     * @param keyMapper   the function to map an element to a key
     * @param valueMapper the function to map an element to a value
     * @return a {@link HashMap}
     */
    public static <T, K, V> Map<K, V> toMap(
            final Iterable<T> iterable,
            final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        return MapKit.putAll(MapKit.newHashMap(), iterable, keyMapper, valueMapper);
    }

    /**
     * Adapts the specified {@link Enumeration} to the {@link Iterator} interface.
     *
     * @param <E> the element type
     * @param e   the {@link Enumeration}
     * @return an {@link Iterator}
     */
    public static <E> Iterator<E> asIterator(final Enumeration<E> e) {
        return new EnumerationIterator<>(Objects.requireNonNull(e));
    }

    /**
     * Converts an {@link Iterator} to an {@link Iterable} that can be used only once.
     *
     * @param <E>  the element type
     * @param iter the {@link Iterator}
     * @return an {@link Iterable}
     */
    public static <E> Iterable<E> asIterable(final Iterator<E> iter) {
        return () -> iter;
    }

    /**
     * Gets the element type of an {@link Iterable} object (determined by the first non-null element). Note that this
     * method will call the next method at least once.
     *
     * @param iterable the {@link Iterable}
     * @return the element type, or {@code null} if the list is empty or all elements are null
     */
    public static Class<?> getElementType(final Iterable<?> iterable) {
        return getElementType(getIter(iterable));
    }

    /**
     * Gets the element type of an {@link Iterator} object (determined by the first non-null element). Note that this
     * method will call the next method at least once.
     *
     * @param iterator the {@link Iterator}
     * @return the element type, or {@code null} if the iterator is {@code null}, empty, or all elements are
     *         {@code null}
     */
    public static Class<?> getElementType(final Iterator<?> iterator) {
        if (null == iterator) {
            return null;
        }
        final Object ele = getFirstNoneNull(iterator);
        return null == ele ? null : ele.getClass();
    }

    /**
     * Edits an iterator, producing a new {@link ArrayList}. The editing process is defined by the provided editor,
     * which can:
     * 
     * <pre>
     * 1. Filter out desired objects (return {@code
     * null
     * } to discard an element).
     * 2. Modify an element object and return the modified object to be included in the new list.
     * </pre>
     *
     * @param <T>    the element type
     * @param iter   the iterator
     * @param editor the editor interface (if {@code null}, no editing is performed)
     * @return the filtered and/or modified list
     */
    public static <T> List<T> edit(final Iterator<T> iter, final UnaryOperator<T> editor) {
        final List<T> result = new ArrayList<>();
        if (null == iter) {
            return result;
        }

        T modified;
        while (iter.hasNext()) {
            modified = (null == editor) ? iter.next() : editor.apply(iter.next());
            if (null != modified) {
                result.add(modified);
            }
        }
        return result;
    }

    /**
     * Removes all elements from the iterator that satisfy the given predicate. This method modifies the underlying
     * collection directly. The removal is done by implementing the {@link Predicate} interface:
     * 
     * <pre>
     * 1. Remove specific objects: objects for which {@link Predicate#test(Object)} returns {@code
     * true
     * } will be removed using {@link Iterator#remove()}.
     * </pre>
     *
     * @param <E>       the element type
     * @param iter      the iterator
     * @param predicate the filter interface; elements for which {@link Predicate#test(Object)} is {@code true} will be
     *                  removed
     * @return the modified iterator
     */
    public static <E> Iterator<E> remove(final Iterator<E> iter, final Predicate<E> predicate) {
        if (null == iter || null == predicate) {
            return iter;
        }

        while (iter.hasNext()) {
            if (predicate.test(iter.next())) {
                iter.remove();
            }
        }
        return iter;
    }

    /**
     * Filters an {@link Iterator} and adds the elements that satisfy the predicate to a new {@link ArrayList}.
     *
     * @param <E>       the element type
     * @param iter      the {@link Iterator}
     * @param predicate the filter; elements for which {@link Predicate#test(Object)} is {@code true} are retained
     * @return an {@link ArrayList} containing the filtered elements
     */
    public static <E> List<E> filterToList(final Iterator<E> iter, final Predicate<E> predicate) {
        return ListKit.of(filtered(iter, predicate));
    }

    /**
     * Returns a new {@link FilterIterator} that filters elements from the given iterator.
     *
     * @param <E>       the element type
     * @param iterator  the iterator to wrap
     * @param predicate the predicate to apply; elements for which {@link Predicate#test(Object)} is {@code true} are
     *                  retained
     * @return a new {@link FilterIterator}
     */
    public static <E> FilterIterator<E> filtered(
            final Iterator<? extends E> iterator,
            final Predicate<? super E> predicate) {
        return new FilterIterator<>(iterator, predicate);
    }

    /**
     * Returns an empty iterator.
     *
     * @param <T> the element type
     * @return an empty iterator
     * @see Collections#emptyIterator()
     */
    public static <T> Iterator<T> empty() {
        return Collections.emptyIterator();
    }

    /**
     * Transforms an {@link Iterator} of one type to an {@link Iterator} of another type using the given function.
     *
     * @param <F>      the source element type
     * @param <T>      the target element type
     * @param iterator the source {@link Iterator}
     * @param function the transformation function
     * @return the transformed {@link Iterator}
     */
    public static <F, T> Iterator<T> trans(
            final Iterator<F> iterator,
            final Function<? super F, ? extends T> function) {
        return new TransIterator<>(iterator, function);
    }

    /**
     * Returns the number of elements in an {@link Iterable}.
     *
     * @param iterable the {@link Iterable}
     * @return the number of elements
     */
    public static int size(final Iterable<?> iterable) {
        if (null == iterable) {
            return 0;
        }

        if (iterable instanceof Collection<?>) {
            return ((Collection<?>) iterable).size();
        } else {
            return size(iterable.iterator());
        }
    }

    /**
     * Returns the number of elements in an {@link Iterator}.
     *
     * @param iterator the {@link Iterator}
     * @return the number of elements
     */
    public static int size(final Iterator<?> iterator) {
        int size = 0;
        if (iterator != null) {
            while (iterator.hasNext()) {
                iterator.next();
                size++;
            }
        }
        return size;
    }

    /**
     * Clears the specified {@link Iterator} by iterating through it and calling {@link Iterator#remove()} on each
     * element.
     *
     * @param iterator the {@link Iterator} to clear
     */
    public static void clear(final Iterator<?> iterator) {
        if (null != iterator) {
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    /**
     * Iterates over an {@link Iterator}. If the consumer is {@code null}, it still iterates but does nothing.
     *
     * @param <E>      the element type
     * @param iterator the {@link Iterator}
     * @param consumer the consumer for each element; if {@code null}, no action is taken
     */
    public static <E> void forEach(final Iterator<E> iterator, final Consumer<? super E> consumer) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                final E element = iterator.next();
                if (null != consumer) {
                    consumer.accept(element);
                }
            }
        }
    }

    /**
     * Iterates over an {@link Iterator}, applying a {@link BiConsumerX} to each element along with its index.
     *
     * @param <T>      the element type
     * @param iterator the {@link Iterator}
     * @param consumer the {@link BiConsumerX} that processes each element with its index
     */
    public static <T> void forEach(final Iterator<T> iterator, final BiConsumerX<Integer, T> consumer) {
        if (iterator == null) {
            return;
        }
        int index = 0;
        while (iterator.hasNext()) {
            consumer.accept(index, iterator.next());
            index++;
        }
    }

    /**
     * Converts an {@link Iterator} to a string.
     *
     * @param <E>      the element type
     * @param iterator the {@link Iterator}
     * @return a string representation of the iterator's elements
     */
    public static <E> String toString(final Iterator<E> iterator) {
        return toString(iterator, Convert::toStringOrNull);
    }

    /**
     * Converts an {@link Iterator} to a string using a transformation function for each element.
     *
     * @param <E>       the element type
     * @param iterator  the {@link Iterator}
     * @param transFunc the function to convert each element to a string
     * @return a string representation of the iterator's elements
     */
    public static <E> String toString(final Iterator<E> iterator, final Function<? super E, String> transFunc) {
        return toString(iterator, transFunc, ", ", "[", "]");
    }

    /**
     * Converts an {@link Iterator} to a string with a specified delimiter, prefix, and suffix.
     *
     * @param <E>       the element type
     * @param iterator  the {@link Iterator}
     * @param transFunc the function to convert each element to a string
     * @param delimiter the delimiter to use between elements
     * @param prefix    the prefix for the entire string
     * @param suffix    the suffix for the entire string
     * @return a string representation of the iterator's elements
     */
    public static <E> String toString(
            final Iterator<E> iterator,
            final Function<? super E, String> transFunc,
            final String delimiter,
            final String prefix,
            final String suffix) {
        final StringJoiner stringJoiner = StringJoiner.of(delimiter, prefix, suffix);
        stringJoiner.append(iterator, transFunc);
        return stringJoiner.toString();
    }

    /**
     * Gets an {@link Iterator} from a given object based on the following rules:
     * <ul>
     * <li>null - returns null</li>
     * <li>Iterator - returns the object itself</li>
     * <li>Enumeration - returns an {@link EnumerationIterator}</li>
     * <li>Collection - calls {@link Collection#iterator()}</li>
     * <li>Map - returns an iterator over the map's entries</li>
     * <li>Dictionary - returns an iterator over the values (elements)</li>
     * <li>array - returns an {@link ArrayIterator}</li>
     * <li>NodeList - returns a {@link NodeListIterator}</li>
     * <li>Node - returns an iterator over its child nodes</li>
     * <li>object with a public iterator() method - accessed via reflection</li>
     * <li>other object - returns an {@link ArrayIterator} containing the single object</li>
     * </ul>
     *
     * @param object the object from which to get an {@link Iterator}
     * @return an {@link Iterator}, or {@code null} if the provided object is {@code null}
     */
    public static Iterator<?> getIter(final Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Iterator) {
            return (Iterator<?>) object;
        } else if (object instanceof Iterable) {
            return ((Iterable<?>) object).iterator();
        } else if (ArrayKit.isArray(object)) {
            return new ArrayIterator<>(object);
        } else if (object instanceof Enumeration) {
            return new EnumerationIterator<>((Enumeration<?>) object);
        } else if (object instanceof Map) {
            return ((Map<?, ?>) object).entrySet().iterator();
        } else if (object instanceof NodeList) {
            return new NodeListIterator((NodeList) object);
        } else if (object instanceof Node) {
            // Iterate over child nodes
            return new NodeListIterator(((Node) object).getChildNodes());
        } else if (object instanceof Dictionary) {
            return new EnumerationIterator<>(((Dictionary<?, ?>) object).elements());
        }

        // Reflection
        try {
            final Object iterator = MethodKit.invoke(object, "iterator");
            if (iterator instanceof Iterator) {
                return (Iterator<?>) iterator;
            }
        } catch (final RuntimeException ignore) {
            // ignore
        }
        return new ArrayIterator<>(new Object[] { object });
    }

}
