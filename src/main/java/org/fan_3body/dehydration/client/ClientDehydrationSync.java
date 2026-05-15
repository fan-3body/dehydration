package org.fan_3body.dehydration.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.fan_3body.dehydration.Dehydration;
import org.fan_3body.dehydration.capability.DehydrationProvider;
import org.fan_3body.dehydration.dehydration.DehydrationStage;

public final class ClientDehydrationSync {
    private ClientDehydrationSync() {
    }

    public static void apply(DehydrationStage state, boolean firstDehydration, boolean firstRehydration) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.getCapability(DehydrationProvider.CAPABILITY).ifPresent(cap -> {
            Dehydration.LOGGER.debug("[3B][CLIENT] Apply sync player={} state={}, firstDehydration={}, firstRehydration={}",
                    Dehydration.playerName(player),
                    state.getSerializedName(),
                    firstDehydration,
                    firstRehydration);
            cap.setState(state);
            cap.setFirstDehydration(firstDehydration);
            cap.setFirstRehydration(firstRehydration);
            if (state.isNormal()) {
                cap.resetTimers();
            } else if (state.isDehydrated()) {
                cap.setRehydratingTicks(0);
            } else {
                cap.setDehydratedTicks(0);
            }
        });
    }
}
