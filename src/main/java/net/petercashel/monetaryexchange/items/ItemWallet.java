package net.petercashel.monetaryexchange.items;

import net.minecraft.world.item.Item;

public class ItemWallet extends Item {
    private final String CurrencyGameID;

    public ItemWallet(Properties properties, String currencyGameID) {
        super(properties);
        CurrencyGameID = currencyGameID;
    }

    public boolean IsCurrencyGameID(String currencyGameID) {
        return currencyGameID.equals(CurrencyGameID);
    }
}
