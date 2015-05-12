package de.bayerl.statistics.transformer;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface NameAnnotation{
    String name();
}
