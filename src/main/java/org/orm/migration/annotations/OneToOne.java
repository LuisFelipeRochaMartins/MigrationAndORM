package org.orm.migration.annotations;

import org.orm.migration.Cascade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {
    Class target() default void.class;
    Cascade[] cascade() default {};
    String column();
}
