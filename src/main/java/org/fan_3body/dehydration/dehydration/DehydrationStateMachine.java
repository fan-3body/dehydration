package org.fan_3body.dehydration.dehydration;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.fan_3body.dehydration.Dehydration;
import org.fan_3body.dehydration.capability.DehydrationCap;
import org.fan_3body.dehydration.capability.DehydrationProvider;
import org.fan_3body.dehydration.config.DehydrationConfig;
import org.fan_3body.dehydration.integration.LsoIntegration;
import org.fan_3body.dehydration.network.DehydrationNetwork;

import java.util.function.Consumer;

public final class DehydrationStateMachine {
    private static final int RECOVERY_STABILITY_TICKS = 40;

    private DehydrationStateMachine() {
    }

    public static void requestEnter(ServerPlayer player) {
        withState(player, state -> {
            if (!state.isNormal()) {
                Dehydration.LOGGER.info("[3B][REQUEST] Enter ignored for {}: state={}",
                        Dehydration.playerName(player),
                        state.getState().getSerializedName());
                return;
            }
            if (DehydrationConfig.requireHeatStrokeToEnter && !LsoIntegration.hasHeatStroke(player)) {
                Dehydration.LOGGER.info("[3B][REQUEST] Enter rejected for {}: requireHeatStroke=true, hasHeatStroke=false",
                        Dehydration.playerName(player));
                player.displayClientMessage(Component.translatable("message.dehydration.requires_heat_stroke"), true);
                return;
            }
            Dehydration.LOGGER.info("[3B][REQUEST] Enter accepted for {}", Dehydration.playerName(player));
            enterDehydrated(player, state);
        });
    }

    public static void requestToggle(ServerPlayer player) {
        withState(player, state -> {
            if (state.isNormal()) {
                requestEnter(player);
                return;
            }

            Dehydration.LOGGER.info("[3B][REQUEST] Exit accepted for {}: state={}",
                    Dehydration.playerName(player),
                    state.getState().getSerializedName());
            enterNormal(player, state, "manual_request");
        });
    }

    public static void tick(ServerPlayer player) {
        withState(player, state -> {
            if (state.isNormal()) {
                return;
            }
            RecoveryCheck recovery = RecoveryCheck.read(player);
            if (state.isDehydrated()) {
                state.incrementDehydratedTicks();
                if (recovery.canRecover()) {
                    state.incrementRecoveryCandidateTicks();
                    if (state.getRecoveryCandidateTicks() >= RECOVERY_STABILITY_TICKS) {
                        enterRehydrating(player, state, recovery);
                    }
                } else {
                    if (state.getRecoveryCandidateTicks() > 0) {
                        Dehydration.LOGGER.debug("[3B][RECOVER] {} recovery candidate reset at {}/{}: heatStroke={}, thirstFull={}",
                                Dehydration.playerName(player),
                                state.getRecoveryCandidateTicks(),
                                RECOVERY_STABILITY_TICKS,
                                recovery.hasHeatStroke(),
                                recovery.thirstFull());
                    }
                    state.setRecoveryCandidateTicks(0);
                }
                return;
            }

            if (!recovery.canRecover()) {
                Dehydration.LOGGER.info("[3B][RECOVER] {} recovery broken: heatStroke={}, thirstFull={}, rehydratingTicks={}/{}",
                        Dehydration.playerName(player),
                        recovery.hasHeatStroke(),
                        recovery.thirstFull(),
                        state.getRehydratingTicks(),
                        DehydrationConfig.rehydrationTicks);
                enterDehydrated(player, state, "recovery_broken");
                return;
            }

            state.incrementRehydratingTicks();
            if (state.getRehydratingTicks() >= DehydrationConfig.rehydrationTicks) {
                enterNormal(player, state, recovery);
            }
        });
    }

    public static void sync(ServerPlayer player) {
        sync(player, "unspecified");
    }

    public static void sync(ServerPlayer player, String reason) {
        withState(player, state -> {
            Dehydration.LOGGER.info("[3B][SYNC] Sync request reason={} player={} state={}, firstDehydration={}, firstRehydration={}",
                    reason,
                    Dehydration.playerName(player),
                    state.getState().getSerializedName(),
                    state.hasFirstDehydration(),
                    state.hasFirstRehydration());
            DehydrationNetwork.syncToClient(player, reason);
        });
    }

    private static void enterDehydrated(ServerPlayer player, DehydrationCap state) {
        enterDehydrated(player, state, "manual_request");
    }

    private static void enterDehydrated(ServerPlayer player, DehydrationCap state, String reason) {
        DehydrationStage previous = state.getState();
        boolean firstEntry = !state.hasFirstDehydration();
        state.setState(DehydrationStage.DEHYDRATED);
        state.setFirstDehydration(true);
        state.setDehydratedTicks(0);
        state.setRehydratingTicks(0);
        state.setRecoveryCandidateTicks(0);
        boolean thirstCleared = LsoIntegration.clearThirst(player);
        LsoIntegration.removeTemperatureDamageImmunity(player);
        Dehydration.LOGGER.info("[3B][STATE] {} {} -> dehydrated reason={} firstDehydration={}",
                Dehydration.playerName(player),
                previous.getSerializedName(),
                reason,
                firstEntry);
        Dehydration.LOGGER.info("[3B][THIRST] Clear thirst on dehydration player={} cleared={}",
                Dehydration.playerName(player),
                thirstCleared);
        DehydrationNetwork.syncToClient(player, "state_change:dehydrated");
        player.displayClientMessage(Component.translatable("message.dehydration.enter"), true);
        if (firstEntry) {
            player.displayClientMessage(Component.translatable("message.dehydration.first_dehydration"), false);
        }
    }

    private static void enterRehydrating(ServerPlayer player, DehydrationCap state, RecoveryCheck recovery) {
        DehydrationStage previous = state.getState();
        boolean firstRecovery = !state.hasFirstRehydration();
        state.setState(DehydrationStage.REHYDRATING);
        state.setFirstRehydration(true);
        state.setDehydratedTicks(0);
        state.setRehydratingTicks(0);
        state.setRecoveryCandidateTicks(0);
        Dehydration.LOGGER.info("[3B][STATE] {} {} -> rehydrating firstRehydration={} thirstFull={} heatStroke={} stabilityTicks={}",
                Dehydration.playerName(player),
                previous.getSerializedName(),
                firstRecovery,
                recovery.thirstFull(),
                recovery.hasHeatStroke(),
                RECOVERY_STABILITY_TICKS);
        DehydrationNetwork.syncToClient(player, "state_change:rehydrating");
        player.displayClientMessage(Component.translatable("message.dehydration.rehydrating"), true);
        if (firstRecovery) {
            player.displayClientMessage(Component.translatable("message.dehydration.first_rehydration"), false);
        }
    }

    private static void enterNormal(ServerPlayer player, DehydrationCap state, RecoveryCheck recovery) {
        enterNormal(player, state, "recovery_complete", recovery);
    }

    private static void enterNormal(ServerPlayer player, DehydrationCap state, String reason) {
        enterNormal(player, state, reason, RecoveryCheck.read(player));
    }

    private static void enterNormal(ServerPlayer player, DehydrationCap state, String reason, RecoveryCheck recovery) {
        DehydrationStage previous = state.getState();
        int finishedTicks = state.getRehydratingTicks();
        state.setState(DehydrationStage.NORMAL);
        state.resetTimers();
        DehydrationEffects.clearAfterExit(player);
        Dehydration.LOGGER.info("[3B][STATE] {} {} -> normal reason={} finishedRehydratingTicks={} thirstFull={} heatStroke={}",
                Dehydration.playerName(player),
                previous.getSerializedName(),
                reason,
                finishedTicks,
                recovery.thirstFull(),
                recovery.hasHeatStroke());
        DehydrationNetwork.syncToClient(player, "state_change:normal");
        player.displayClientMessage(Component.translatable("message.dehydration.exit"), true);
    }

    private static void withState(ServerPlayer player, Consumer<DehydrationCap> action) {
        player.getCapability(DehydrationProvider.CAPABILITY).ifPresent(state -> action.accept(state));
    }

    private record RecoveryCheck(boolean hasHeatStroke, boolean thirstFull) {
        private static RecoveryCheck read(ServerPlayer player) {
            return new RecoveryCheck(LsoIntegration.hasHeatStroke(player), LsoIntegration.isThirstFull(player));
        }

        private boolean canRecover() {
            return !hasHeatStroke && thirstFull;
        }
    }
}
