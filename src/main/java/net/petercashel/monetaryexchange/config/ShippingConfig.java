package net.petercashel.monetaryexchange.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.conversion.SpecNotNull;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.petercashel.monetaryexchange.MonetaryExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = MonetaryExchange.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ShippingConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SHIPPING_ENABLED = BUILDER
            .comment("Enables the Shipping Bins. ")
            .define("SHIPPING_BIN_ENABLED", true);


    private static final ModConfigSpec.ConfigValue<List<? extends SynchronizedConfig>> LIST_SHIPPING_ENTRY = BUILDER
            .worldRestart()
            .defineList(
                    "SHIPPING_ENTRY",
                    () -> {
                        SynchronizedConfig config = SynchronizedConfig.convert(Config.inMemory());
                        config.set("ItemName", "minecraft:iron_ingot");
                        config.set("Count", 4);
                        config.set("Value", 8);

                        SynchronizedConfig config2 = SynchronizedConfig.convert(Config.inMemory());
                        config2.set("ItemName", "minecraft:iron_ingot");
                        config2.set("Count", 8);
                        config2.set("Value", 17);
                        return List.of(config, config2);
                    },
                    () -> {
                        SynchronizedConfig config = SynchronizedConfig.convert(Config.inMemory());
                        config.set("ItemName", "minecraft:iron_ingot");
                        config.set("Count", 4);
                        config.set("Value", 8);
                        return config;
                    },
                    ShippingConfig::validateShippingItemFormat)
            ;

    private static boolean validateShippingItemFormat(final Object obj)
    {
        if (obj instanceof SynchronizedConfig config) {
            String s = config.get("ItemName");
            return  s != null &&
                    config.getInt("Count") > 0 &&
                    config.getInt("Value") > 0 &&
                    BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(s))
                    ;
        }

        return false;
    }

    public static class ShippingEntry {

        private ResourceLocation _ResourceLocation;
        public String ItemName;
        public int Count;
        public int Value;
        private Optional<Holder.Reference<Item>> _ItemHolder;

        public ShippingEntry() {

        }
        public ShippingEntry(String itemName, int count, int value) {
            ItemName = itemName;
            Count = count;
            Value = value;

            _ResourceLocation = ResourceLocation.parse(ItemName);
        }

        public Optional<Holder.Reference<Item>> GetItem() {
            if (_ItemHolder == null || _ItemHolder.isEmpty()) {
                _ItemHolder = BuiltInRegistries.ITEM.getHolder(_ResourceLocation);
            }
            return _ItemHolder;
        }

        public ResourceLocation GetResourceLocation() {
            return _ResourceLocation;
        }


        public static ShippingEntry FromSyncConfig(SynchronizedConfig c) {
            return new ShippingEntry(c.get("ItemName"), c.getInt("Count"), c.getInt("Value"));
        }

        @Override
        public String toString() {
            return "ShippingEntry [ItemName=" + ItemName + ", Count=" + Count + ", Value=" + Value + "]";
        }

        public String toDisplayString() {
            if (GetItem().isPresent()) {
                Item item = GetItem().get().value();
                String displayName = item.getName(new ItemStack(item, Count)).getString();

                return Count + "x " + displayName + " @ " + Value;
            }
            return Count + "x " + ItemName + " @ " + Value;
        }
    }


    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean Shipping_Enabled;
    public static List<ShippingEntry> Shipping_Entries;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getFileName().equals("monetaryexchange-shipping-common.toml")) {
            Shipping_Entries = new ArrayList<>();
            Shipping_Enabled = SHIPPING_ENABLED.get();

            for (SynchronizedConfig c : LIST_SHIPPING_ENTRY.get()) {
                if (c.contains("ItemName") && c.contains("Count") && c.contains("Value")) {
                    Shipping_Entries.add(new ShippingEntry(c.get("ItemName"), c.getInt("Count"), c.getInt("Value")));
                }
            }
        }
    }
}
