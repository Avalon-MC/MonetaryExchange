package net.petercashel.monetaryexchange.api.interfaces;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface IMonetaryExchangeAPI {

    public List<String> GetCurrencyGameIDs();

    public ResourceLocation GetCurrencyItem();
    public ResourceLocation GetCurrencyItem(String currencyID);



    public boolean HasWallet(Player player);
    public boolean HasWallet(String currencyID, Player player);

    public boolean HasCurrency(Player player, double amount);
    public boolean HasCurrency(String currencyID, Player player, double amount);

    public double GetCurrency(Player player, double amount);
    public double GetCurrency(String currencyID, Player player, double amount);

    public boolean TakeCurrency(Player player, double amount);
    public boolean TakeCurrency(String currencyID, Player player, double amount);

    public boolean GiveCurrency(Player player, double amount);
    public boolean GiveCurrency(String currencyID, Player player, double amount);
}
