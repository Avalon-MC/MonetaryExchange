package net.petercashel.monetaryexchange.items;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public class ItemCoin extends Item {
    private final String CurrencyGameID;
    @Nullable
    private String descriptionId;

    public ItemCoin(Properties properties, String currencyGameID) {
        super(properties);
        CurrencyGameID = currencyGameID;
    }
}
