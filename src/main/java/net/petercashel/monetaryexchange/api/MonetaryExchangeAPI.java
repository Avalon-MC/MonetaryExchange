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
    private static String _DefaultCurrencyID = "default_coin";

    /*
     * DO NOT HARD CODE THIS VALUE, DO REFERENCE THIS GETTER. IT MAY BECOME CONFIGURABLE IN FUTURE AND THE VALUE MAY CHANGE
     */
    public static String GetDefaultCurrencyID() {
        return _DefaultCurrencyID;
    }
}
