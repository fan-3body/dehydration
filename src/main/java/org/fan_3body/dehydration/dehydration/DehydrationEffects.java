package org.fan_3body.dehydration.dehydration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;
import org.fan_3body.dehydration.integration.LsoIntegration;

public final class DehydrationEffects {
    private static final int EFFECT_DURATION_TICKS = 20 * 8;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 20 * 3;
    private static final ResourceLocation LSO_HEAT_THIRST_EFFECT_ID =
            ResourceLocation.fromNamespaceAndPath(LsoIntegration.LSO_MODID, "heat_thirst");

    private DehydrationEffects() {
    }

    public static void apply(ServerPlayer player) {
        DehydrationAttributeModifiers.apply(player);
        applyEffectIfNeeded(player, MobEffects.MOVEMENT_SLOWDOWN, 0, true, true);
        applyEffectIfNeeded(player, MobEffects.DIG_SLOWDOWN, 0, true, true);
        keepLsoDehydrationIconVisible(player);
    }

    public static void clear(ServerPlayer player) {
        DehydrationAttributeModifiers.remove(player);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
    }

    public static void clearAfterExit(ServerPlayer player) {
        clear(player);
        MobEffect heatThirst = ForgeRegistries.MOB_EFFECTS.getValue(LSO_HEAT_THIRST_EFFECT_ID);
        if (heatThirst != null) {
            player.removeEffect(heatThirst);
        }
    }

    private static void applyEffectIfNeeded(ServerPlayer player, MobEffect effect, int amplifier, boolean visible, boolean showIcon) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && current.getAmplifier() >= amplifier && current.getDuration() > EFFECT_REFRESH_THRESHOLD_TICKS
                && current.isVisible() == visible && current.showIcon() == showIcon) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION_TICKS, amplifier, false, visible, showIcon));
    }

    private static void keepLsoDehydrationIconVisible(ServerPlayer player) {
        MobEffect heatThirst = ForgeRegistries.MOB_EFFECTS.getValue(LSO_HEAT_THIRST_EFFECT_ID);
        if (heatThirst == null) {
            return;
        }

        MobEffectInstance current = player.getEffect(heatThirst);
        if (current == null || current.showIcon()) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                heatThirst,
                Math.max(current.getDuration(), EFFECT_REFRESH_THRESHOLD_TICKS),
                current.getAmplifier(),
                current.isAmbient(),
                current.isVisible(),
                true));
    }
}
