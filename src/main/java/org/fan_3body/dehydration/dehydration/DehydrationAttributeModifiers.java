package org.fan_3body.dehydration.dehydration;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class DehydrationAttributeModifiers {
    private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("5d7a5d7e-7d2b-4d9b-9ad6-c0b7ad67d501");
    private static final String MAX_HEALTH_MODIFIER_NAME = "Three-body dehydration max health";
    private static final double DEHYDRATED_MAX_HEALTH = 10.0D;

    private DehydrationAttributeModifiers() {
    }

    public static void apply(Player player) {
        if (player == null) {
            return;
        }
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
        double modifierAmount = DEHYDRATED_MAX_HEALTH - maxHealth.getValue();
        if (Math.abs(modifierAmount) > 0.001D) {
            maxHealth.addTransientModifier(new AttributeModifier(
                    MAX_HEALTH_MODIFIER_ID,
                    MAX_HEALTH_MODIFIER_NAME,
                    modifierAmount,
                    AttributeModifier.Operation.ADDITION));
        }

        if (player.getHealth() > DEHYDRATED_MAX_HEALTH) {
            player.setHealth((float) DEHYDRATED_MAX_HEALTH);
        }
    }

    public static void remove(Player player) {
        if (player == null) {
            return;
        }
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
        }
    }
}
