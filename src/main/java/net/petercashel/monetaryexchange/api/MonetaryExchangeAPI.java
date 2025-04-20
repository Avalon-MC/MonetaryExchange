package net.petercashel.monetaryexchange.api;

import net.petercashel.monetaryexchange.api.implementation.MonetaryExchangeAPI_Implem;
import net.petercashel.monetaryexchange.api.interfaces.IMonetaryExchangeAPI;

public class MonetaryExchangeAPI {

    /*
     * The current implementation of the API backend.
     */
    public static IMonetaryExchangeAPI API_Instance = new MonetaryExchangeAPI_Implem();

    /*
    * DO NOT HARD CODE THIS VALUE. IT MAY BECOME CONFIGURABLE IN FUTURE
    */
    private static String _DefaultCurrencyID = "defaultCoin";

    /*
     * DO NOT HARD CODE THIS VALUE. IT MAY BECOME CONFIGURABLE IN FUTURE
     */
    public static String GetDefaultCurrencyID() {
        return _DefaultCurrencyID;
    }
}
