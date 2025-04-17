package net.petercashel.monetaryexchange.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBField {
    /**
     * Column Name
     */
    public String ColumnName();

    /**
     * Max length for VARCHAR type fields
     */
    public int MaxLength() default 250;


    public boolean Nullable() default false;


    public int Decimal_Precision() default 20;
    public int Decimal_Scale() default 2;


    public ColumnDataTypeEnum DataType();
}
