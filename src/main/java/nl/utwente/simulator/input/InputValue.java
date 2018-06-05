package nl.utwente.simulator.input;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to give the string representation of a class for class selection, for the purpose of user input
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InputValue {
    String value() default "";
    InputSource[] src() default {InputSource.UNSTRUCTURED};
}
