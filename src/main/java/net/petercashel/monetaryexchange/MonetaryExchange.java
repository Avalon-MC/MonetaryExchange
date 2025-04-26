package net.petercashel.monetaryexchange;

import com.google.gson.Gson;
import com.mysql.cj.jdbc.MysqlDataSource;
import dev.latvian.mods.kubejs.bindings.event.NetworkEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.petercashel.monetaryexchange.api.MonetaryExchangeAPI;
import net.petercashel.monetaryexchange.database.internals.DB_Initializer;
import net.petercashel.monetaryexchange.items.ItemCoin;
import net.petercashel.monetaryexchange.items.ItemWallet;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MonetaryExchange.MODID)
public class MonetaryExchange
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "monetaryexchange";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "monetaryexchange" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "monetaryexchange" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "monetaryexchange" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<ItemCoin> DEFAULT_COIN_ITEM = ITEMS.registerItem("default_coin", (p) -> new ItemCoin(p, MonetaryExchangeAPI.GetDefaultCurrencyID()), new Item.Properties());
    public static final DeferredItem<ItemWallet> DEFAULT_WALLET_ITEM = ITEMS.registerItem("default_wallet", (p) -> new ItemWallet(p, MonetaryExchangeAPI.GetDefaultCurrencyID()), new Item.Properties());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MONETARY_EXCHANGE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.monetaryexchange")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> DEFAULT_COIN_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(DEFAULT_COIN_ITEM.get());
                output.accept(DEFAULT_WALLET_ITEM.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MonetaryExchange(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (MonetaryExchange) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }


    private void commonSetup(final FMLCommonSetupEvent event)
    {


    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == MONETARY_EXCHANGE_TAB.getKey()) {
            //event.accept(DEFAULT_WALLET_ITEM);
        }

    }

    public static Sql2o Sql2o_Instance = null;
    public static boolean DBIsEmbedded = false;
    public static Gson InternalGSONInstance = new Gson();

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        if (Config.Db_Enabled) {

            MysqlDataSource ds = new MysqlDataSource();
            ds.setServerName(Config.Db_Host);
            ds.setPort(Config.Db_Port);
            ds.setDatabaseName(Config.Db_Database);
            ds.setUser(Config.Db_User);
            ds.setPassword(Config.Db_Pass);

            Sql2o_Instance = new Sql2o(ds);
        } else {
            DBIsEmbedded = true;
            // Do something when the server starts
            Path root = event.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            Path worldDBPath = root.resolve("database").normalize();
            if (!worldDBPath.startsWith(root)) {
                throw new IllegalArgumentException("Potential Path Traversal Attack");
            }
            try {
                Files.createDirectories(worldDBPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:" + worldDBPath.toString() + "/monetaryExchange.h2db");

            ds.setUser("sa");
            ds.setPassword("sa");


            Sql2o_Instance = new Sql2o(ds);
        }



        DB_Initializer.Prepare(Sql2o_Instance);


    }

    @SubscribeEvent
    public void OnPlayerJoined(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        Player p = playerLoggedInEvent.getEntity();
        DB_Initializer.playerProfileManager.EnsurePlayerCreated(p);
    }


    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        //Clean up
    }
    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event)
    {
        Sql2o_Instance = null;
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code

        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
    public static class DedicatedServerModEvents
    {
        @SubscribeEvent
        public static void onDedicatedServerSetup(FMLDedicatedServerSetupEvent event)
        {
            // Some server setup code

        }
    }
}
