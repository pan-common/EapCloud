package com.taiji.eap.common.dictionary.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dictionary {

    long parentId() default -1;

}
