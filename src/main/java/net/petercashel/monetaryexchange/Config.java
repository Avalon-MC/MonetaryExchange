package net.petercashel.monetaryexchange;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = MonetaryExchange.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue DB_ENABLED = BUILDER.push("database")
            .comment("DB_ENABLED")
            .define("DB_ENABLED", false);

    private static final ModConfigSpec.ConfigValue<String> DB_HOST = BUILDER
            .comment("DB_HOST")
            .define("DB_HOST", "localhost");

    private static final ModConfigSpec.IntValue DB_PORT = BUILDER
            .comment("DB_PORT")
            .defineInRange("DB_PORT", 3306, 0, 32767);

    private static final ModConfigSpec.ConfigValue<String> DB_DATABASE = BUILDER
            .comment("DB_DATABASE")
            .define("DB_DATABASE", "monetary_exchange");

    private static final ModConfigSpec.ConfigValue<String> DB_USERNAME = BUILDER
            .comment("DB_USERNAME")
            .define("DB_USERNAME", "monetary_exchange");

    private static final ModConfigSpec.ConfigValue<String> DB_PASSWORD = BUILDER
            .comment("DB_PASSWORD")
            .define("DB_PASSWORD", "monetary_exchange");


    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean Db_Enabled;
    public static int Db_Port;
    public static String Db_Host;
    public static String Db_Database;
    public static String Db_User;
    public static String Db_Pass;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        Db_Enabled = DB_ENABLED.get();
        Db_Port = DB_PORT.get();

        Db_Host = DB_HOST.get();
        Db_Database = DB_DATABASE.get();
        Db_User = DB_USERNAME.get();
        Db_Pass = DB_PASSWORD.get();
    }
}
