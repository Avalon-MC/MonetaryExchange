package net.petercashel.monetaryexchange.api.implementation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.petercashel.monetaryexchange.api.MonetaryExchangeAPI;
import net.petercashel.monetaryexchange.api.interfaces.IMonetaryExchangeAPI;

import java.util.ArrayList;
import java.util.List;

//TODO
/*
TODO
 */
public class MonetaryExchangeAPI_Implem implements IMonetaryExchangeAPI {

    @Override
    public List<String> GetCurrencyIDs() {
        return new ArrayList<>();
    }

    @Override
    public ResourceLocation GetCurrencyItem(String currencyID) {
        return null;
    }

    @Override
    public boolean HasWallet(String currencyID, Player player) {
        return false;
    }

    @Override
    public boolean HasCurrency(String currencyID, Player player, double amount) {
        return false;
    }

    @Override
    public double GetCurrency(String currencyID, Player player, double amount) {
        return 0;
    }

    @Override
    public boolean TakeCurrency(String currencyID, Player player, double amount) {
        return false;
    }

    @Override
    public boolean GiveCurrency(String currencyID, Player player, double amount) {
        return false;
    }




    @Override
    public ResourceLocation GetCurrencyItem() {
        return GetCurrencyItem(MonetaryExchangeAPI.GetDefaultCurrencyID());
    }

    @Override
    public boolean HasWallet(Player player) {
        return HasWallet(MonetaryExchangeAPI.GetDefaultCurrencyID(), player);
    }

    @Override
    public boolean HasCurrency(Player player, double amount) {
        return HasCurrency(MonetaryExchangeAPI.GetDefaultCurrencyID(), player, amount);
    }

    @Override
    public double GetCurrency(Player player, double amount) {
        return GetCurrency(MonetaryExchangeAPI.GetDefaultCurrencyID(), player, amount);
    }

    @Override
    public boolean TakeCurrency(Player player, double amount) {
        return TakeCurrency(MonetaryExchangeAPI.GetDefaultCurrencyID(), player, amount);
    }

    @Override
    public boolean GiveCurrency(Player player, double amount) {
        return GiveCurrency(MonetaryExchangeAPI.GetDefaultCurrencyID(), player, amount);
    }
}
