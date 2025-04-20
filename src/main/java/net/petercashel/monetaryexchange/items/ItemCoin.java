package net.petercashel.monetaryexchange.items;

import net.minecraft.world.item.Item;

public class ItemCoin extends Item {
    private final String CurrencyGameID;

    public ItemCoin(Properties properties, String currencyGameID) {
        super(properties);
        CurrencyGameID = currencyGameID;
    }
}
