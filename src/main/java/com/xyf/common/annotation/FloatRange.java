package com.xyf.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE})
public @interface FloatRange {
    double from() default Double.MIN_VALUE;

    double to() default Double.MAX_VALUE;

    boolean fromInclusive() default true;

    boolean toInclusive() default true;
}
