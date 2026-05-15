package org.fan_3body.dehydration.api;

import net.minecraft.world.entity.player.Player;
import org.fan_3body.dehydration.capability.DehydrationCap;
import org.fan_3body.dehydration.capability.DehydrationProvider;
import org.fan_3body.dehydration.dehydration.DehydrationStage;

import java.util.Optional;

public final class DehydrationApi {
    private DehydrationApi() {
    }

    public static boolean isDehydrated(Player player) {
        return getCap(player).map(DehydrationCap::isDehydrated).orElse(false);
    }

    public static String getState(Player player) {
        return getCap(player)
                .map(DehydrationCap::getState)
                .map(DehydrationStage::getSerializedName)
                .orElse(DehydrationStage.NORMAL.getSerializedName());
    }

    public static boolean isFirstDehydration(Player player) {
        return getCap(player).map(DehydrationCap::hasFirstDehydration).orElse(false);
    }

    public static boolean isFirstRehydration(Player player) {
        return getCap(player).map(DehydrationCap::hasFirstRehydration).orElse(false);
    }

    private static Optional<DehydrationCap> getCap(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return player.getCapability(DehydrationProvider.CAPABILITY).resolve();
    }
}
