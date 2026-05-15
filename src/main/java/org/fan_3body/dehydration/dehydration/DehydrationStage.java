package org.fan_3body.dehydration.dehydration;

public enum DehydrationStage {
    NORMAL("normal"),
    DEHYDRATED("dehydrated"),
    REHYDRATING("rehydrating");

    private final String serializedName;

    DehydrationStage(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }

    public boolean isDehydrated() {
        return this == DEHYDRATED;
    }

    public boolean isRehydrating() {
        return this == REHYDRATING;
    }

    public static DehydrationStage fromSerializedName(String serializedName) {
        for (DehydrationStage stage : values()) {
            if (stage.serializedName.equals(serializedName)) {
                return stage;
            }
        }
        return NORMAL;
    }
}
