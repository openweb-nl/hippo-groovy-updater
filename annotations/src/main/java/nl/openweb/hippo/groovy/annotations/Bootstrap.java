package nl.openweb.hippo.groovy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bootstrap annotation to define properties in the hippoecm-extension.xml
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bootstrap {

    String contentroot() default "queue";
    double sequence() default 99999.0d;
    boolean reload() default false;
    String version() default "";
}
