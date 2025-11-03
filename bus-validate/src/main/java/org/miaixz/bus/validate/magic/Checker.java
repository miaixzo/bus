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
package org.miaixz.bus.validate.magic;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.NoSuchException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.validate.*;
import org.miaixz.bus.validate.magic.annotation.Inside;
import org.miaixz.bus.validate.magic.annotation.NotBlank;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The validation checker. This class is responsible for orchestrating the validation process based on the provided
 * {@link Verified} object and its associated {@link Material} rules.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Checker {

    /**
     * Validates an object against a specific validator rule.
     *
     * @param verified The object to be validated, wrapped in a {@link Verified} instance.
     * @param material The validation rule material.
     * @return A {@link Collector} containing the validation results.
     * @throws ValidateException if fast-fail is enabled in the context and validation fails.
     */
    public Collector object(Verified verified, Material material) throws ValidateException {
        Collector collector = new Collector(verified);
        Context context = verified.getContext();

        if (Provider.isGroup(material.getGroup(), context.getGroup())) {
            collector.collect(doObject(verified, material));
        }
        List<Material> list = material.getList();
        for (Material p : list) {
            collector.collect(doObject(verified, p));
        }
        return collector;
    }

    /**
     * Performs a deep validation on all fields of the object within the {@link Verified} instance.
     *
     * @param verified The object to be validated.
     * @return A {@link Collector} containing the validation results for all internal fields.
     */
    public Collector inside(Verified verified) {
        Collector collector = new Collector(verified);

        Object object = verified.getObject();
        if (ObjectKit.isEmpty(object)) {
            Logger.debug(
                    true,
                    "Checker",
                    "The verified object is null, skip validation of internal fields: {}",
                    verified);
            return collector;
        }

        Field[] fields = FieldKit.getFields(object.getClass());
        for (Field field : fields) {
            Object value = FieldKit.getFieldValue(object, field);
            Annotation[] annotations = field.getDeclaredAnnotations();
            String[] xFields = verified.getContext().getField();
            String[] xSkip = null == verified.getContext().getSkip() ? null : verified.getContext().getSkip();
            // Filter out fields that should be skipped.
            if (ArrayKit.isNotEmpty(xSkip) && Arrays.asList(xSkip).contains(field.getName())) {
                continue;
            }
            // Filter for fields that should be validated.
            if (ArrayKit.isNotEmpty(xFields) && !Arrays.asList(xFields).contains(field.getName())) {
                continue;
            }

            // Start field validation.
            verified.getContext().setInside(false);
            verified = new Verified(value, annotations, verified.getContext(), field.getName());
            if (null != value && Provider.isCollection(value) && hasInside(annotations)) {
                collector.collect(doCollectionInside(verified));
            } else if (null != value && Provider.isArray(value) && hasInside(annotations)) {
                collector.collect(doArrayInside(verified));
            }

            if (verified.getList().isEmpty()) {
                Logger.warn(true, "Checker", "Please check the annotation on property: {}", field.getName());
                // Create a Verified object with a default Material.
                verified = new Verified(value, new Annotation[0], verified.getContext(), field.getName());
                verified.getList().add(without(field));
            }

            collector.collect(verified.access());
        }
        return collector;
    }

    /**
     * Creates a {@link Material} object representing a "not blank" validation rule.
     *
     * @param field The field to be validated.
     * @return A {@link Material} object configured with the validation rule.
     */
    public Material without(Field field) {
        // Create a new Material object to store the validation rule.
        Material material = new Material();

        // Set the validator name to "NOT_BLANK".
        material.setName(Builder._NOT_BLANK);

        // Get the NotBlank validator class from the Registry and set it.
        // The require method is used here to ensure the validator exists.
        material.setClazz(Registry.getInstance().require(Builder._NOT_BLANK).getClass());

        // Create a NotBlank annotation instance using a dynamic proxy.
        // The proxy will return the default values for the annotation methods.
        material.setAnnotation(
                (NotBlank) Proxy.newProxyInstance(
                        // Use the annotation's class loader.
                        NotBlank.class.getClassLoader(),
                        // The implemented interface.
                        new Class<?>[] { NotBlank.class },
                        // The invocation handler.
                        (proxy, method, args) -> {
                            // Return the default value of the annotation method.
                            return method.getDefaultValue();
                        }));

        // Set the error message template, using the ${field} placeholder.
        material.setErrmsg("Please check the ${field} parameter");

        // Set the default error code.
        material.setErrcode(Builder.DEFAULT_ERRCODE);

        // Set the name of the field to be validated.
        material.setField(field.getName());

        // Set the validation groups to an empty array (meaning no group).
        material.setGroup(new String[0]);

        // Add validation parameters:
        // 1. FIELD parameter: the field name.
        material.addParam(Builder.FIELD, field.getName());
        // 2. VALUE parameter: an empty string (for validating against null/empty).
        material.addParam(Builder.VALUE, Normal.EMPTY);

        return material;
    }

    /**
     * Performs the actual validation of an object against a rule.
     *
     * @param verified The object to be validated.
     * @param material The validation rule material.
     * @return A {@link Collector} containing the validation result.
     */
    public Collector doObject(Verified verified, Material material) {
        Matcher matcher = (Matcher) Registry.getInstance().require(material.getName(), material.getClazz());
        if (ObjectKit.isEmpty(matcher)) {
            throw new NoSuchException(String.format(
                    "Cannot find the specified validator, name:%s, class:%s",
                    material.getName(),
                    null == material.getClazz() ? Normal.NULL : material.getClazz().getName()));
        }
        Object validatedTarget = verified.getObject();
        if (ObjectKit.isNotEmpty(validatedTarget) && material.isArray() && Provider.isArray(validatedTarget)) {
            return doArrayObject(verified, material);
        } else if (ObjectKit.isNotEmpty(validatedTarget) && material.isArray()
                && Provider.isCollection(validatedTarget)) {
            return doCollection(verified, material);
        } else {
            boolean result = matcher.on(validatedTarget, material.getAnnotation(), verified.getContext());
            if (!result && verified.getContext().isFast()) {
                throw Provider.resolve(material, verified.getContext());
            }
            return new Collector(verified, material, result);
        }
    }

    /**
     * Validates each element of a collection.
     *
     * @param verified The collection object to be validated.
     * @param material The validation rule to apply to each element.
     * @return A {@link Collector} containing the results for each element.
     */
    public Collector doCollection(Verified verified, Material material) {
        Collector collector = new Collector(verified);
        Collection<?> collection = (Collection<?>) verified.getObject();
        for (Object item : collection) {
            Verified itemTarget = new Verified(item, new Annotation[] { material.getAnnotation() },
                    verified.getContext());
            Collector checked = itemTarget.access();
            collector.collect(checked);
        }

        return collector;
    }

    /**
     * Validates each element of an array.
     *
     * @param verified The array object to be validated.
     * @param material The validation rule to apply to each element.
     * @return A {@link Collector} containing the results for each element.
     */
    public Collector doArrayObject(Verified verified, Material material) {
        Collector collector = new Collector(verified);
        Object[] array = (Object[]) verified.getObject();
        for (int i = 0; i < array.length; i++) {
            Verified itemTarget = new Verified(array[i], new Annotation[] { material.getAnnotation() },
                    verified.getContext());
            Collector checked = itemTarget.access();
            collector.collect(checked);
        }
        return collector;
    }

    /**
     * Performs a deep validation on each element of an array.
     *
     * @param verified The array object to be validated.
     * @return A {@link Collector} containing the deep validation results for each element.
     */
    public Collector doArrayInside(Verified verified) {
        Collector collector = new Collector(verified);
        Object[] array = (Object[]) verified.getObject();
        for (Object object : array) {
            collector.collect(inside(new Verified(object, verified.getContext())));
        }
        return collector;
    }

    /**
     * Performs a deep validation on each element of a collection.
     *
     * @param verified The collection object to be validated.
     * @return A {@link Collector} containing the deep validation results for each element.
     */
    private Collector doCollectionInside(Verified verified) {
        Collector collector = new Collector(verified);
        Collection<?> collection = (Collection<?>) verified.getObject();
        for (Object item : collection) {
            collector.collect(inside(new Verified(item, verified.getContext())));
        }
        return collector;
    }

    /**
     * Checks if the given array of annotations contains the {@link Inside} annotation.
     *
     * @param annotations The array of annotations to check.
     * @return {@code true} if {@link Inside} is present, {@code false} otherwise.
     */
    public boolean hasInside(Annotation[] annotations) {
        return Arrays.stream(annotations).anyMatch(an -> an instanceof Inside);
    }

}
