package net.petercashel.monetaryexchange;

import com.google.gson.Gson;
import com.mysql.cj.jdbc.MysqlDataSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.*;
import net.petercashel.monetaryexchange.api.MonetaryExchangeAPI;
import net.petercashel.monetaryexchange.blocks.sellingbin.common.SellingBinEnderAttachment;
import net.petercashel.monetaryexchange.blocks.sellingbin.iron.SellingBinIronBlock;
import net.petercashel.monetaryexchange.blocks.sellingbin.iron.SellingBinIronBlockEntity;
import net.petercashel.monetaryexchange.blocks.sellingbin.shipping.ShippingContainerBlock;
import net.petercashel.monetaryexchange.blocks.sellingbin.shipping.ShippingContainerBlockEntity;
import net.petercashel.monetaryexchange.blocks.sellingbin.wood.SellingBinWoodBlock;
import net.petercashel.monetaryexchange.blocks.sellingbin.wood.SellingBinWoodBlockEntity;
import net.petercashel.monetaryexchange.blocks.sellingbin.diamond.SellingBinDiamondBlock;
import net.petercashel.monetaryexchange.blocks.sellingbin.diamond.SellingBinDiamondBlockEntity;
import net.petercashel.monetaryexchange.client.ShippingConfigurationSectionScreen;
import net.petercashel.monetaryexchange.config.DatabaseConfig;
import net.petercashel.monetaryexchange.config.ShippingConfig;
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
import org.sql2o.Sql2o;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MonetaryExchange.MODID)
public class MonetaryExchange
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "monetaryexchange";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "monetaryexchange" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Create the DeferredRegister for attachment types
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);



    public static final DeferredBlock<SellingBinWoodBlock> SELLING_BIN_WOOD = BLOCKS.register(
            "selling_bin_wood",
            (registryName) -> new SellingBinWoodBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .destroyTime(2.0f)
                    .explosionResistance(10.0f)
                    .sound(SoundType.WOOD)
            ));
    public static final DeferredBlock<SellingBinIronBlock> SELLING_BIN_IRON = BLOCKS.register(
            "selling_bin_iron",
            (registryName) -> new SellingBinIronBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .destroyTime(3.0f)
                    .explosionResistance(10.0f)
                    .sound(SoundType.METAL)
            ));
    public static final DeferredBlock<SellingBinDiamondBlock> SELLING_BIN_DIAMOND = BLOCKS.register(
            "selling_bin_diamond",
            (registryName) -> new SellingBinDiamondBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .destroyTime(4.0f)
                    .explosionResistance(10.0f)
                    .sound(SoundType.GLASS)
            ));


    public static final Supplier<AttachmentType<SellingBinEnderAttachment>> SELLING_BIN_WOOD_HANDLER = ATTACHMENT_TYPES.register(
            "selling_bin_wood_handler", () -> AttachmentType.serializable(() -> new SellingBinEnderAttachment(18)).build()
    );
    public static final Supplier<AttachmentType<SellingBinEnderAttachment>> SELLING_BIN_IRON_HANDLER = ATTACHMENT_TYPES.register(
            "selling_bin_iron_handler", () -> AttachmentType.serializable(() -> new SellingBinEnderAttachment(27)).build()
    );
    public static final Supplier<AttachmentType<SellingBinEnderAttachment>> SELLING_BIN_DIAMOND_HANDLER = ATTACHMENT_TYPES.register(
            "selling_bin_diamond_handler", () -> AttachmentType.serializable(() -> new SellingBinEnderAttachment(36)).build()
    );


    public static final Supplier<BlockEntityType<SellingBinWoodBlockEntity>> SELLING_BIN_WOOD_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "selling_bin_wood_block_entity",
            // The block entity type.
            () -> BlockEntityType.Builder.of(
                    // The supplier to use for constructing the block entity instances.
                    SellingBinWoodBlockEntity::new,
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    MonetaryExchange.SELLING_BIN_WOOD.get()
            ).build(null)
    );
    public static final Supplier<BlockEntityType<SellingBinIronBlockEntity>> SELLING_BIN_IRON_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "selling_bin_iron_block_entity",
            // The block entity type.
            () -> BlockEntityType.Builder.of(
                    // The supplier to use for constructing the block entity instances.
                    SellingBinIronBlockEntity::new,
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    MonetaryExchange.SELLING_BIN_IRON.get()
            ).build(null)
    );
    public static final Supplier<BlockEntityType<SellingBinDiamondBlockEntity>> SELLING_BIN_DIAMOND_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "selling_bin_diamond_block_entity",
            // The block entity type.
            () -> BlockEntityType.Builder.of(
                    // The supplier to use for constructing the block entity instances.
                    SellingBinDiamondBlockEntity::new,
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    MonetaryExchange.SELLING_BIN_DIAMOND.get()
            ).build(null)
    );



    public static final DeferredBlock<ShippingContainerBlock> SHIPPING_CONTAINER = BLOCKS.register(
            "shipping_container",
            (registryName) -> new ShippingContainerBlock(BlockBehaviour.Properties.of()
                    .destroyTime(4.0f)
                    .explosionResistance(10.0f)
                    .sound(SoundType.METAL)
            ));

    public static final Supplier<BlockEntityType<ShippingContainerBlockEntity>> SHIPPING_CONTAINER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "shipping_container_block_entity",
            // The block entity type.
            () -> BlockEntityType.Builder.of(
                    // The supplier to use for constructing the block entity instances.
                    ShippingContainerBlockEntity::new,
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    MonetaryExchange.SHIPPING_CONTAINER.get()
            ).build(null)
    );

    public static final Supplier<BlockItem> SELLING_BIN_WOOD_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            "selling_bin_wood",
            SELLING_BIN_WOOD, new Item.Properties()
    );
    public static final Supplier<BlockItem> SELLING_BIN_IRON_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            "selling_bin_iron",
            SELLING_BIN_IRON, new Item.Properties()
    );
    public static final Supplier<BlockItem> SELLING_BIN_DIAMOND_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            "selling_bin_diamond",
            SELLING_BIN_DIAMOND, new Item.Properties()
    );
    public static final Supplier<BlockItem> SHIPPING_CONTAINER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            "shipping_container",
            SHIPPING_CONTAINER, new Item.Properties()
    );

    public static final DeferredItem<ItemCoin> DEFAULT_COIN_ITEM = ITEMS.registerItem("default_coin", (p) -> new ItemCoin(p, MonetaryExchangeAPI.GetDefaultCurrencyID()), new Item.Properties());
    public static final DeferredItem<ItemWallet> DEFAULT_WALLET_ITEM = ITEMS.registerItem("default_wallet", (p) -> new ItemWallet(p, MonetaryExchangeAPI.GetDefaultCurrencyID()), new Item.Properties().stacksTo(1));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MONETARY_EXCHANGE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.monetaryexchange")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> DEFAULT_COIN_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(DEFAULT_COIN_ITEM.get());
                output.accept(DEFAULT_WALLET_ITEM.get());
                output.accept(SELLING_BIN_WOOD_BLOCK_ITEM.get());
                output.accept(SELLING_BIN_IRON_BLOCK_ITEM.get());
                output.accept(SELLING_BIN_DIAMOND_BLOCK_ITEM.get());
                output.accept(SHIPPING_CONTAINER_BLOCK_ITEM.get());
            }).build());








    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MonetaryExchange(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        ATTACHMENT_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (MonetaryExchange) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, DatabaseConfig.SPEC, getConfigName(ModConfig.Type.COMMON, "database"));
        modContainer.registerConfig(ModConfig.Type.COMMON, ShippingConfig.SPEC, getConfigName(ModConfig.Type.COMMON, "shipping"));

        if (FMLEnvironment.dist.isClient()) {
            //Future Todo, extend this screen to support our shipping item list.
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> {
                //noinspection Convert2MethodRef
                return new net.neoforged.neoforge.client.gui.ConfigurationScreen(mc, parent,
                (configurationScreen, configType, modConfig, component) -> {
                    if (modConfig.getFileName().equals("monetaryexchange-shipping-common.toml")) {
                        //This is the root screen for the config
                        return new ShippingConfigurationSectionScreen(configurationScreen, configType, modConfig, component);
                    }
                    return new ConfigurationScreen.ConfigurationSectionScreen(configurationScreen, configType, modConfig, component);
                }
                );
            });
        }
    }

    private static String getConfigName(ModConfig.Type type, String fileName) {
        // for mod-id "forge", config file name would be "forge-client.toml" and "forge-server.toml"
        return String.format(Locale.ROOT, "%s-%s-%s.toml", MODID, fileName.toLowerCase(Locale.ROOT), type.extension());
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
        if (DatabaseConfig.Db_Enabled) {

            MysqlDataSource ds = new MysqlDataSource();
            ds.setServerName(DatabaseConfig.Db_Host);
            ds.setPort(DatabaseConfig.Db_Port);
            ds.setDatabaseName(DatabaseConfig.Db_Database);
            ds.setUser(DatabaseConfig.Db_User);
            ds.setPassword(DatabaseConfig.Db_Pass);

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
