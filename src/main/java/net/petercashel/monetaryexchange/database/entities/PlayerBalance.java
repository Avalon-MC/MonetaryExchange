package net.petercashel.monetaryexchange.database.entities;

import net.petercashel.monetaryexchange.database.annotations.*;

@DBTable(TableName = "tbl_PlayerBalances")
public class PlayerBalance {

    @DBKey
    @DBField(ColumnName = "balanceID", DataType = ColumnDataTypeEnum.INTEGER)
    public int ID;

    @DBField(ColumnName = "playerID", DataType = ColumnDataTypeEnum.INTEGER)
    @DBForeignKey(ForeignTableName = "tbl_PlayerProfiles", ForeignColumnName = "playerID")
    public int PlayerID;

    @DBField(ColumnName = "currencyID", DataType = ColumnDataTypeEnum.INTEGER)
    @DBForeignKey(ForeignTableName = "tbl_CurrencyTypes", ForeignColumnName = "currencyID")
    public int CurrencyID;

    @DBField(ColumnName = "balance", DataType = ColumnDataTypeEnum.DOUBLE)
    public double Balance;

    public PlayerBalance() {
    }

    public PlayerBalance(int playerID, int currencyID) {
        PlayerID = playerID;
        CurrencyID = currencyID;
    }

    public PlayerBalance(int playerID, int currencyID, double balance) {
        PlayerID = playerID;
        CurrencyID = currencyID;
        Balance = balance;
    }
}
