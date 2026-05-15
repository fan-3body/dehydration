package org.fan_3body.dehydration;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.fan_3body.dehydration.config.DehydrationConfig;
import org.fan_3body.dehydration.network.DehydrationNetwork;
import org.fan_3body.dehydration.util.LSOHelper;
import org.slf4j.Logger;

@Mod(Dehydration.MODID)
public final class Dehydration {
    public static final String MODID = "dehydration";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Dehydration() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DehydrationConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LSOHelper.init();
            DehydrationNetwork.register();
        });
        LOGGER.info("Dehydration Mode initialized");
    }

    public static String playerName(Player player) {
        if (player == null) {
            return "null-player";
        }

        GameProfile profile = player.getGameProfile();
        if (profile != null && profile.getName() != null && !profile.getName().isBlank()) {
            return profile.getName();
        }

        return "player#" + player.getStringUUID();
    }
}
