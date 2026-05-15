package org.fan_3body.dehydration.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.fan_3body.dehydration.Dehydration;
import org.fan_3body.dehydration.dehydration.DehydrationStage;

@AutoRegisterCapability
public final class DehydrationCap {
    private static final String STATE_KEY = "State";
    private static final String LEGACY_DEHYDRATED_KEY = "IsDehydrated";
    private static final String FIRST_DEHYDRATION_KEY = "FirstDehydration";
    private static final String FIRST_REHYDRATION_KEY = "FirstRehydration";
    private static final String DEHYDRATED_TICKS_KEY = "DehydratedTicks";
    private static final String REHYDRATING_TICKS_KEY = "RehydratingTicks";
    private static final String RECOVERY_CANDIDATE_TICKS_KEY = "RecoveryCandidateTicks";

    private DehydrationStage state = DehydrationStage.NORMAL;
    private boolean firstDehydration;
    private boolean firstRehydration;
    private int dehydratedTicks;
    private int rehydratingTicks;
    private int recoveryCandidateTicks;

    public DehydrationStage getState() {
        return state;
    }

    public void setState(DehydrationStage state) {
        this.state = state == null ? DehydrationStage.NORMAL : state;
    }

    public boolean isDehydrated() {
        return state.isDehydrated();
    }

    public boolean isRehydrating() {
        return state.isRehydrating();
    }

    public boolean isNormal() {
        return state.isNormal();
    }

    public boolean hasFirstDehydration() {
        return firstDehydration;
    }

    public void setFirstDehydration(boolean firstDehydration) {
        this.firstDehydration = firstDehydration;
    }

    public boolean hasFirstRehydration() {
        return firstRehydration;
    }

    public void setFirstRehydration(boolean firstRehydration) {
        this.firstRehydration = firstRehydration;
    }

    public int getDehydratedTicks() {
        return dehydratedTicks;
    }

    public void setDehydratedTicks(int dehydratedTicks) {
        this.dehydratedTicks = Math.max(0, dehydratedTicks);
    }

    public void incrementDehydratedTicks() {
        dehydratedTicks++;
    }

    public int getRehydratingTicks() {
        return rehydratingTicks;
    }

    public void setRehydratingTicks(int rehydratingTicks) {
        this.rehydratingTicks = Math.max(0, rehydratingTicks);
    }

    public void incrementRehydratingTicks() {
        rehydratingTicks++;
    }

    public int getRecoveryCandidateTicks() {
        return recoveryCandidateTicks;
    }

    public void setRecoveryCandidateTicks(int recoveryCandidateTicks) {
        this.recoveryCandidateTicks = Math.max(0, recoveryCandidateTicks);
    }

    public void incrementRecoveryCandidateTicks() {
        recoveryCandidateTicks++;
    }

    public void resetTimers() {
        dehydratedTicks = 0;
        rehydratingTicks = 0;
        recoveryCandidateTicks = 0;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(STATE_KEY, state.getSerializedName());
        tag.putBoolean(FIRST_DEHYDRATION_KEY, firstDehydration);
        tag.putBoolean(FIRST_REHYDRATION_KEY, firstRehydration);
        tag.putInt(DEHYDRATED_TICKS_KEY, dehydratedTicks);
        tag.putInt(REHYDRATING_TICKS_KEY, rehydratingTicks);
        tag.putInt(RECOVERY_CANDIDATE_TICKS_KEY, recoveryCandidateTicks);
        Dehydration.LOGGER.debug("[3B][NBT] serialize state={}, firstDehydration={}, firstRehydration={}, dehydratedTicks={}, rehydratingTicks={}, recoveryCandidateTicks={}",
                state.getSerializedName(),
                firstDehydration,
                firstRehydration,
                dehydratedTicks,
                rehydratingTicks,
                recoveryCandidateTicks);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag == null) {
            state = DehydrationStage.NORMAL;
            firstDehydration = false;
            firstRehydration = false;
            resetTimers();
            return;
        }

        if (tag.contains(STATE_KEY)) {
            state = DehydrationStage.fromSerializedName(tag.getString(STATE_KEY));
        } else if (tag.getBoolean(LEGACY_DEHYDRATED_KEY)) {
            state = DehydrationStage.DEHYDRATED;
        } else {
            state = DehydrationStage.NORMAL;
        }

        firstDehydration = tag.getBoolean(FIRST_DEHYDRATION_KEY);
        firstRehydration = tag.getBoolean(FIRST_REHYDRATION_KEY);
        dehydratedTicks = Math.max(0, tag.getInt(DEHYDRATED_TICKS_KEY));
        rehydratingTicks = Math.max(0, tag.getInt(REHYDRATING_TICKS_KEY));
        recoveryCandidateTicks = Math.max(0, tag.getInt(RECOVERY_CANDIDATE_TICKS_KEY));

        if (state.isNormal()) {
            resetTimers();
        } else if (state.isDehydrated()) {
            rehydratingTicks = 0;
        } else {
            dehydratedTicks = 0;
            recoveryCandidateTicks = 0;
        }

        Dehydration.LOGGER.debug("[3B][NBT] deserialize state={}, firstDehydration={}, firstRehydration={}, dehydratedTicks={}, rehydratingTicks={}, recoveryCandidateTicks={}",
                state.getSerializedName(),
                firstDehydration,
                firstRehydration,
                dehydratedTicks,
                rehydratingTicks,
                recoveryCandidateTicks);
    }
}
