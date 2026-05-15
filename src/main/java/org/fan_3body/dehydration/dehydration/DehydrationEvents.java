package org.fan_3body.dehydration.dehydration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.fan_3body.dehydration.Dehydration;
import org.fan_3body.dehydration.capability.DehydrationProvider;
import org.fan_3body.dehydration.network.DehydrationNetwork;

@Mod.EventBusSubscriber(modid = Dehydration.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DehydrationEvents {
    private DehydrationEvents() {
    }

    @SubscribeEvent
    public static void attachPlayerCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            DehydrationProvider provider = new DehydrationProvider();
            event.addCapability(DehydrationProvider.ID, provider);
            event.addListener(provider::invalidate);
            Dehydration.LOGGER.debug("[3B][NBT] Attach capability for {}", Dehydration.playerName(player));
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(DehydrationProvider.CAPABILITY).ifPresent(oldState ->
                event.getEntity().getCapability(DehydrationProvider.CAPABILITY).ifPresent(newState -> {
                    newState.deserializeNBT(oldState.serializeNBT());
                    Dehydration.LOGGER.info("[3B][NBT] Clone copy for {}: state={}, firstDehydration={}, firstRehydration={}",
                            event.getEntity().getName().getString(),
                            newState.getState().getSerializedName(),
                            newState.hasFirstDehydration(),
                            newState.hasFirstRehydration());
                }));
        event.getOriginal().invalidateCaps();
        syncIfServerPlayer(event.getEntity(), "clone");
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        syncIfServerPlayer(event.getEntity(), "logout");
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        syncIfServerPlayer(event.getEntity(), "login");
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncIfServerPlayer(event.getEntity(), "dimension");
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncIfServerPlayer(event.getEntity(), "respawn");
    }

    private static void syncIfServerPlayer(Player player, String reason) {
        if (player instanceof ServerPlayer serverPlayer) {
            DehydrationStateMachine.sync(serverPlayer, reason);
        }
    }
}
