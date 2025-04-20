package net.petercashel.monetaryexchange.database.entities;

import net.petercashel.monetaryexchange.database.annotations.ColumnDataTypeEnum;
import net.petercashel.monetaryexchange.database.annotations.DBField;
import net.petercashel.monetaryexchange.database.annotations.DBKey;
import net.petercashel.monetaryexchange.database.annotations.DBTable;

@DBTable(TableName = "tbl_CurrencyTypes")
public class CurrencyType {

    @DBKey
    @DBField(ColumnName = "currencyID", DataType = ColumnDataTypeEnum.INTEGER)
    public int currencyID;

    @DBField(ColumnName = "currencyGameID", DataType = ColumnDataTypeEnum.VARCHAR, MaxLength = 48)
    public String CurrencyGameID;


    @DBField(ColumnName = "currencyItem", DataType = ColumnDataTypeEnum.VARCHAR, MaxLength = 250)
    public String CurrencyItem;

    public CurrencyType() {
    }

    public CurrencyType(String currencyGameID) {
        CurrencyGameID = currencyGameID;
    }

    public CurrencyType(String currencyGameID, String ItemResourceLocation) {
        CurrencyGameID = currencyGameID;
        CurrencyItem = ItemResourceLocation;
    }
}
