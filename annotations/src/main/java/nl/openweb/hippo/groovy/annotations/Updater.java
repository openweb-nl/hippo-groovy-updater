package nl.openweb.hippo.groovy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Updater {
    String name();
    String description() default "";
    String path() default "";
    String xpath() default "";
    long batchSize() default 10L;
    boolean dryRun() default false;
    String parameters() default "";
    long throttle() default 1000L;
}
