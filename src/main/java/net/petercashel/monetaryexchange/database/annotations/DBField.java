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
     * Column Type
     */
    public ColumnDataTypeEnum DataType();

    /**
     * Max length for VARCHAR type fields. default 250.
     */
    public int MaxLength() default 250;

    /**
     * Are null values allowed. default false.
     */
    public boolean Nullable() default false;


    /**
     * Total Digits including decimal places. Number less than 50. default 20.
     */
    public int NUMERIC_Precision() default 20;

    /**
     * Digits after decimal place. Between 0 - 4 is preferred. default 2.
     */
    public int NUMERIC_Scale() default 2;

    /**
     * Should we update the stored date on change. Only valid for ColumnDataTypeEnum.LOCALDATETIME columns. default false.
     */
    public boolean UpdateDateOnChange() default false;
}
