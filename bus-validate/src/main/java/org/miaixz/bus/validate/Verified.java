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
package org.miaixz.bus.validate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.validate.magic.Checker;
import org.miaixz.bus.validate.magic.Criterion;
import org.miaixz.bus.validate.magic.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an object to be validated. Note: When the object to be validated is null, its Class cannot be obtained, so
 * any validation annotations marked on the class will not be executed.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class Verified extends Provider {

    /**
     * List of validation criterions.
     */
    private List<Criterion> list;
    /**
     * The value of the property being validated.
     */
    private Object object;
    /**
     * The name of the property being validated.
     */
    private String field;
    /**
     * The validation context.
     */
    private Context context;

    /**
     * Constructs a Verified object with a default validator context.
     *
     * @param object The original object to be validated.
     */
    public Verified(Object object) {
        this.object = object;
        this.context = resolve(Context.newInstance(), new Annotation[0]);
        this.list = new ArrayList<>();
    }

    /**
     * Constructs a Verified object with a parent context.
     *
     * @param object        The original object to be validated.
     * @param parentContext The parent validation context. The current validation environment will inherit all parent
     *                      context information except for the property indicating whether to validate the object's
     *                      internals.
     */
    public Verified(Object object, Context parentContext) {
        this.object = object;
        this.context = resolve(parentContext, new Annotation[0]);
        this.list = new ArrayList<>();
    }

    /**
     * Constructs a Verified object with a default validator context.
     *
     * @param object      The original object to be validated.
     * @param annotations All annotations on the object to be validated.
     */
    public Verified(Object object, Annotation[] annotations) {
        this.object = object;
        this.context = resolve(Context.newInstance(), annotations);
        this.list = resolve(annotations);
    }

    /**
     * Constructs a Verified object.
     *
     * @param object      The original object to be validated.
     * @param annotations All annotations on the object to be validated.
     * @param context     The parent validation context. The current validation environment will inherit all parent
     *                    context information except for the property indicating whether to validate the object's
     *                    internals.
     */
    public Verified(Object object, Annotation[] annotations, Context context) {
        this.object = object;
        this.context = resolve(context, annotations);
        this.list = resolve(annotations);
    }

    /**
     * Constructs a Verified object.
     *
     * @param object      The original object to be validated.
     * @param annotations All annotations on the object to be validated.
     * @param context     The parent validation context. The current validation environment will inherit all parent
     *                    context information except for the property indicating whether to validate the object's
     *                    internals.
     * @param field       The property information.
     */
    public Verified(Object object, Annotation[] annotations, Context context, String field) {
        this.field = field;
        this.object = object;
        this.context = resolve(context, annotations);
        this.list = resolve(annotations);
    }

    /**
     * Resolves validators based on object annotations.
     *
     * @param annotations The annotation information.
     * @return A list of resolved validation criterions.
     */
    private List<Criterion> resolve(Annotation[] annotations) {
        List<Criterion> list = new ArrayList<>();
        for (Annotation annotation : annotations) {
            if (isAnnotation(annotation)) {
                Criterion criterion = build(annotation, this.object);
                list.add(criterion);
            }
        }
        if (ObjectKit.isNotEmpty(this.object)) {
            Class<?> clazz = this.object.getClass();
            List<Annotation> clazzAnnotations = getAnnotation(clazz);
            for (Annotation annotation : clazzAnnotations) {
                Criterion criterion = build(annotation, this.object);
                list.add(criterion);
            }
        }
        return list;
    }

    /**
     * Configures the validation context based on object annotations.
     *
     * @param context     The context.
     * @param annotations The annotation information.
     * @return The configured context.
     */
    private Context resolve(Context context, Annotation[] annotations) {
        if (ObjectKit.isNotEmpty(this.object)) {
            Class<?> clazz = this.object.getClass();
            Inside inside = clazz.getAnnotation(Inside.class);
            if (ObjectKit.isNotEmpty(inside)) {
                context.setInside(true);
            }
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof Valid) {
                context.setInside(((Valid) annotation).inside());
                context.setField(((Valid) annotation).value());
                context.setSkip(((Valid) annotation).skip());
            } else if (annotation instanceof Group) {
                context.addGroups(((Group) annotation).value());
            } else if (annotation instanceof ValidEx) {
                context.setException(((ValidEx) annotation).value());
            } else if (annotation instanceof Inside) {
                context.setInside(true);
            }
        }
        return context;
    }

    /**
     * Executes the validation. If the fast-fail property is set to true in the validation context, an exception will be
     * thrown immediately upon the first validation failure.
     *
     * @return The validation result collector.
     */
    public Collector access() {
        Collector collector = new Collector(this);
        Checker checker = context.getChecker();
        for (Criterion p : this.list) {
            Collector result = checker.object(this, p);
            collector.collect(result);
        }
        if (context.isInside()) {
            Collector result = checker.inside(this);
            collector.collect(result);
        }
        return collector;
    }

    /**
     * Creates a validation criterion object.
     *
     * @param annotation The annotation.
     * @param object     The object.
     * @return The validation criterion object.
     */
    public Criterion build(Annotation annotation, Object object) {
        Assert.isTrue(
                isAnnotation(annotation),
                "Attempt to get information from a non-validation annotation:" + annotation);
        Class<? extends Annotation> annotationType = annotation.annotationType();
        try {
            String[] groups = (String[]) annotationType.getMethod(Builder.GROUP).invoke(annotation);
            String errmsg = (String) annotationType.getMethod(Consts.ERRMSG).invoke(annotation);
            String errcode = (String) annotationType.getMethod(Consts.ERRCODE).invoke(annotation);
            String name = (String) annotationType.getMethod(Builder.FIELD).invoke(annotation);
            this.field = Builder.DEFAULT_FIELD.equals(name) ? this.field : name;
            Criterion criterion = new Criterion();
            criterion.setAnnotation(annotation);
            criterion.setErrcode(errcode);
            criterion.setErrmsg(errmsg);
            criterion.setField(this.field);
            criterion.setGroup(groups);
            criterion.addParam(Builder.FIELD, this.field);
            if (ObjectKit.isNotEmpty(object) && object.getClass().isArray()) {
                criterion.addParam(Builder.VALUE, Arrays.toString((Object[]) object));
            } else {
                criterion.addParam(Builder.VALUE, String.valueOf(object));
            }
            Method[] declaredMethods = annotationType.getDeclaredMethods();
            for (Method m : declaredMethods) {
                Filler filler = m.getAnnotation(Filler.class);
                if (ObjectKit.isNotEmpty(filler)) {
                    Class<?> returnType = m.getReturnType();
                    Object invoke = m.invoke(annotation);
                    if (returnType.isArray()) {
                        criterion.addParam(filler.value(), Arrays.toString((Object[]) invoke));
                    } else {
                        criterion.addParam(filler.value(), invoke);
                    }
                }
            }
            Annotation[] parentAnnos = annotationType.getAnnotations();
            for (Annotation anno : parentAnnos) {
                if (isAnnotation(anno)) {
                    criterion.addParentProperty(build(anno, object));
                } else if (anno instanceof Array) {
                    criterion.setArray(true);
                } else if (anno instanceof Complex) {
                    criterion.setClazz(((Complex) anno).clazz());
                    criterion.setName(((Complex) anno).value());
                } else if (anno instanceof ValidEx) {
                    criterion.setException(((ValidEx) anno).value());
                }
            }
            if (ObjectKit.isEmpty(criterion.getClazz()) || StringKit.isEmpty(criterion.getName())) {
                throw new InternalException(
                        "Invalid validation annotation, missing Complex meta-annotation to specify validator:"
                                + annotationType.getName());
            }
            return criterion;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new InternalException(
                    "Invalid validation annotation, missing common validation attributes:" + annotationType.getName(),
                    e);
        }
    }

}
