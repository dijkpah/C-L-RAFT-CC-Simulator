package nl.utwente.simulator.input;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to describe an input field for user input
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Input {
    String value() default "";
    InputSource[] src() default {InputSource.STRUCTURED, InputSource.UNSTRUCTURED};
}
