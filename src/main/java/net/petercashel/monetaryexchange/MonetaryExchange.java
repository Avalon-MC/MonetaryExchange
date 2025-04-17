package net.petercashel.monetaryexchange;

import com.mysql.cj.jdbc.MysqlDataSource;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.petercashel.monetaryexchange.database.migration.DBMigration;
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

    }

    public static Sql2o Sql2o_Instance = null;
    public static boolean DBIsEmbedded = false;

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


        DBMigration.Prepare(Sql2o_Instance);


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
