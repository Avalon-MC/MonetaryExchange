package net.petercashel.monetaryexchange.api.implementation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.petercashel.monetaryexchange.api.MonetaryExchangeAPI;
import net.petercashel.monetaryexchange.api.interfaces.IMonetaryExchangeAPI;
import net.petercashel.monetaryexchange.database.internals.DB_Initializer;
import net.petercashel.monetaryexchange.items.ItemWallet;

import java.util.List;
import java.util.Optional;

/*
The Monetary Exchange API Implementation. Reference this from net.petercashel.monetaryexchange.api.MonetaryExchangeAPI
 */
public class MonetaryExchangeAPI_Implem implements IMonetaryExchangeAPI {

    @Override
    public List<String> GetCurrencyGameIDs() {
        return DB_Initializer.currencyTypeManager.GetCurrencyGameIDs();
    }

    @Override
    public ResourceLocation GetCurrencyItem(String currencyGameID) {
        return ResourceLocation.parse(DB_Initializer.currencyTypeManager.GetCurrencyItem(currencyGameID));
    }

    @Override
    public boolean HasWallet(String currencyGameID, Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemWallet wallet) {
                if (wallet.IsCurrencyGameID(currencyGameID)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean HasCurrency(String currencyGameID, Player player, double amount) {
        DB_Initializer.playerProfileManager.EnsurePlayerCreated(player);
        Optional<Integer> playerID = DB_Initializer.playerProfileManager.GetPlayerID(player);
        if (playerID.isPresent()) {
            Optional<Integer> currencyID = DB_Initializer.currencyTypeManager.GetCurrencyID(currencyGameID);
            if (currencyID.isPresent()) {
                double balance = DB_Initializer.balanceManager.GetPlayerBalance(playerID.get(), currencyID.get());
                return balance >= amount;
            }
        }
        return false;
    }

    @Override
    public double GetCurrency(String currencyGameID, Player player, double amount) {
        DB_Initializer.playerProfileManager.EnsurePlayerCreated(player);
        Optional<Integer> playerID = DB_Initializer.playerProfileManager.GetPlayerID(player);
        if (playerID.isPresent()) {
            Optional<Integer> currencyID = DB_Initializer.currencyTypeManager.GetCurrencyID(currencyGameID);
            if (currencyID.isPresent()) {
                return DB_Initializer.balanceManager.GetPlayerBalance(playerID.get(), currencyID.get());
            }
        }
        return 0;
    }

    @Override
    public boolean TakeCurrency(String currencyGameID, Player player, double amount) {
        DB_Initializer.playerProfileManager.EnsurePlayerCreated(player);
        Optional<Integer> playerID = DB_Initializer.playerProfileManager.GetPlayerID(player);
        if (playerID.isPresent()) {
            Optional<Integer> currencyID = DB_Initializer.currencyTypeManager.GetCurrencyID(currencyGameID);
            if (currencyID.isPresent()) {
                double balance = DB_Initializer.balanceManager.GetPlayerBalance(playerID.get(), currencyID.get());
                if (balance < amount) {
                    return false;
                }
                balance -= amount;
                return DB_Initializer.balanceManager.SetPlayerBalance(playerID.get(), currencyID.get(), balance);
            }
        }
        return false;
    }

    @Override
    public boolean GiveCurrency(String currencyGameID, Player player, double amount) {
        DB_Initializer.playerProfileManager.EnsurePlayerCreated(player);
        Optional<Integer> playerID = DB_Initializer.playerProfileManager.GetPlayerID(player);
        if (playerID.isPresent()) {
            Optional<Integer> currencyID = DB_Initializer.currencyTypeManager.GetCurrencyID(currencyGameID);
            if (currencyID.isPresent()) {
                double balance = DB_Initializer.balanceManager.GetPlayerBalance(playerID.get(), currencyID.get());
                balance += amount;
                return DB_Initializer.balanceManager.SetPlayerBalance(playerID.get(), currencyID.get(), balance);
            }
        }
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
