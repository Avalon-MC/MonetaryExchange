package net.petercashel.monetaryexchange.client;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.petercashel.monetaryexchange.config.ShippingConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ShippingConfigurationSectionScreen extends ConfigurationScreen.ConfigurationSectionScreen {
    public ShippingConfigurationSectionScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
    }

    @Override
    protected @Nullable <T> Element createList(String key, ModConfigSpec.ListValueSpec spec, ModConfigSpec.ConfigValue<List<T>> list) {
        String SECTION = "neoforge.configuration.uitext.section";
        String CRUMB = "neoforge.configuration.uitext.breadcrumb.order";
        Component CRUMB_SEPARATOR = Component.translatable("neoforge.configuration.uitext.breadcrumb.separator").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        if (key.equals("SHIPPING_ENTRY")) {
            return new Element(Component.translatable(SECTION, getTranslationComponent(key)), getTooltipComponent(key, null),
                    Button.builder(Component.translatable(SECTION, Component.translatable(getTranslationKey(key) + ".button")),
                                    button -> minecraft.setScreen(sectionCache.computeIfAbsent(key,
                                            k -> new ShippingConfigurationListScreen(Context.list(context, this), key, Component.translatable(CRUMB, this.getTitle(), CRUMB_SEPARATOR, getTranslationComponent(key)), spec, list).rebuild())))
                            .tooltip(Tooltip.create(getTooltipComponent(key, null))).build(),
                    false);
        } else {
            return super.createList(key,spec, list);
        }
    }

    public class ShippingConfigurationListScreen extends ConfigurationScreen.ConfigurationListScreen<SynchronizedConfig> {

        private static final String LANG_PREFIX = "neoforge.configuration.uitext.";
        public static final Component UNSUPPORTED_ELEMENT = Component.translatable(LANG_PREFIX + "unsupportedelement").withStyle(ChatFormatting.RED);

        public ShippingConfigurationListScreen(Context context, String key, Component title, ModConfigSpec.ListValueSpec spec, ModConfigSpec.ConfigValue valueList) {
            super(context, key, title, spec, valueList);
        }

        @Override
        protected @Nullable Element createOtherValue(int idx, SynchronizedConfig entry) {
            ShippingConfig.ShippingEntry shippingEntry = ShippingConfig.ShippingEntry.FromSyncConfig(entry);

            final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.literal(shippingEntry.toDisplayString()), font).alignLeft();
            label.setTooltip(Tooltip.create(UNSUPPORTED_ELEMENT));
            return new Element(getTranslationComponent(key), getTooltipComponent(key, null), label, false);

        }

        @Override
        protected AbstractWidget createListLabel(int idx) {
            return super.createListLabel(idx);
        }

        @Override
        protected ModConfigSpec.@Nullable ValueSpec getValueSpec(String key) {
            return super.getValueSpec(key);
        }

        @Override
        protected ConfigurationScreen.ConfigurationSectionScreen rebuild() {
            return super.rebuild();
        }
    }
}
