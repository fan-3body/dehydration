package org.fan_3body.dehydration.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.fan_3body.dehydration.Dehydration;

import java.util.List;

@Mod.EventBusSubscriber(modid = Dehydration.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DehydrationConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> HEAT_STROKE_EFFECTS = BUILDER
            .comment("Mob effects that count as Legendary Survival Overhaul heat stroke.")
            .defineListAllowEmpty("heatStrokeEffects",
                    List.of("legendarysurvivaloverhaul:heat_stroke", "legendarysurvivaloverhaul:heatstroke"),
                    DehydrationConfig::isResourceLocation);

    private static final ForgeConfigSpec.BooleanValue REQUIRE_HEAT_STROKE_TO_ENTER = BUILDER
            .comment("If true, the server only allows K to enter dehydration mode while heat stroke is active.")
            .define("requireHeatStrokeToEnter", true);

    private static final ForgeConfigSpec.IntValue REHYDRATION_TICKS = BUILDER
            .comment("How long recovery conditions must hold before returning to normal.")
            .defineInRange("rehydrationTicks", 20 * 5, 20, 20 * 60 * 10);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static List<? extends String> heatStrokeEffects = List.of("legendarysurvivaloverhaul:heat_stroke", "legendarysurvivaloverhaul:heatstroke");
    public static boolean requireHeatStrokeToEnter = true;
    public static int rehydrationTicks = 20 * 5;

    private DehydrationConfig() {
    }

    private static boolean isResourceLocation(Object value) {
        return value instanceof String text && ResourceLocation.isValidResourceLocation(text);
    }

    @SubscribeEvent
    static void onConfigLoad(ModConfigEvent event) {
        heatStrokeEffects = HEAT_STROKE_EFFECTS.get();
        requireHeatStrokeToEnter = REQUIRE_HEAT_STROKE_TO_ENTER.get();
        rehydrationTicks = REHYDRATION_TICKS.get();
    }
}
