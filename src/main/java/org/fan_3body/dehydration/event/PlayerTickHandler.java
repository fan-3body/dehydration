package org.fan_3body.dehydration.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.fan_3body.dehydration.Dehydration;
import org.fan_3body.dehydration.capability.DehydrationProvider;
import org.fan_3body.dehydration.dehydration.DehydrationEffects;
import org.fan_3body.dehydration.dehydration.DehydrationStateMachine;
import org.fan_3body.dehydration.integration.LsoIntegration;

@Mod.EventBusSubscriber(modid = Dehydration.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerTickHandler {
    private static final ResourceKey<DamageType> LSO_DEHYDRATION_DAMAGE = lsoDamageType("dehydration");
    private static final ResourceKey<DamageType> LSO_HYPERTHERMIA_DAMAGE = lsoDamageType("hyperthermia");
    private static final ResourceKey<DamageType> LSO_HYPOTHERMIA_DAMAGE = lsoDamageType("hypothermia");

    private PlayerTickHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        player.getCapability(DehydrationProvider.CAPABILITY).ifPresent(cap -> {
            DehydrationStateMachine.tick(player);
            if (cap.isDehydrated()) {
                DehydrationEffects.apply(player);
            } else {
                DehydrationEffects.clear(player);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        DamageSource source = event.getSource();
        player.getCapability(DehydrationProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.isDehydrated() && isBlockedDehydrationDamage(source)) {
                Dehydration.LOGGER.info("[3B][DAMAGE] Block attack player={} source={} amount={} state={}",
                        Dehydration.playerName(player),
                        source.getMsgId(),
                        event.getAmount(),
                        cap.getState().getSerializedName());
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        String sourceId = event.getSource().getMsgId();
        boolean blockedCandidate = isBlockedDehydrationDamage(event.getSource());

        player.getCapability(DehydrationProvider.CAPABILITY).ifPresent(cap -> {
            if (!cap.isDehydrated()) {
                if (blockedCandidate) {
                    Dehydration.LOGGER.info("[3B][DAMAGE] Allow player={} source={} amount={} state={} reason=not_dehydrated",
                            Dehydration.playerName(player),
                            sourceId,
                            event.getAmount(),
                            cap.getState().getSerializedName());
                }
                return;
            }

            if (blockedCandidate) {
                Dehydration.LOGGER.info("[3B][DAMAGE] Block player={} source={} amount={} state={}",
                        Dehydration.playerName(player),
                        sourceId,
                        event.getAmount(),
                        cap.getState().getSerializedName());
                event.setCanceled(true);
                return;
            }

            Dehydration.LOGGER.info("[3B][DAMAGE] Allow player={} source={} amount={} state={} reason=non_temperature_or_thirst",
                    Dehydration.playerName(player),
                    sourceId,
                    event.getAmount(),
                    cap.getState().getSerializedName());
        });
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DehydrationStateMachine.sync(player, "wake");
        }
    }

    private static boolean isBlockedDehydrationDamage(DamageSource source) {
        return source.is(LSO_DEHYDRATION_DAMAGE)
                || source.is(LSO_HYPERTHERMIA_DAMAGE)
                || source.is(LSO_HYPOTHERMIA_DAMAGE)
                || source.getMsgId().toLowerCase().contains("thirst");
    }

    private static ResourceKey<DamageType> lsoDamageType(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(LsoIntegration.LSO_MODID, path));
    }

}
