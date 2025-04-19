package net.petercashel.monetaryexchange.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBForeignKey {
    /**
     * Column Name
     */
    public String ForeignTableName() default "";
    /**
     * Column Name
     */
    public String ForeignColumnName() default "";
}
