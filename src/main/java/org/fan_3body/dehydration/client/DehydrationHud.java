package org.fan_3body.dehydration.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.fan_3body.dehydration.Dehydration;
import org.fan_3body.dehydration.capability.DehydrationProvider;

@Mod.EventBusSubscriber(modid = Dehydration.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class DehydrationHud {
    private static final ResourceLocation LSO_TEMPERATURE = ResourceLocation.fromNamespaceAndPath("legendarysurvivaloverhaul", "temperature");
    private static final ResourceLocation LSO_TEMPERATURE_OVERLAY = ResourceLocation.fromNamespaceAndPath("legendarysurvivaloverhaul", "temperature_overlay");

    private DehydrationHud() {
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "dehydration_indicator", DehydrationHud::render);
    }

    @Mod.EventBusSubscriber(modid = Dehydration.MODID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static final class ForgeEvents {
        private ForgeEvents() {
        }

        @SubscribeEvent
        public static void hideLsoTemperature(RenderGuiOverlayEvent.Pre event) {
            if (!isDehydrated()) {
                return;
            }
            ResourceLocation overlayId = event.getOverlay().id();
            if (LSO_TEMPERATURE.equals(overlayId) || LSO_TEMPERATURE_OVERLAY.equals(overlayId)) {
                event.setCanceled(true);
            }
        }
    }

    private static void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (Minecraft.getInstance().options.hideGui || !gui.shouldDrawSurvivalElements() || !isDehydrated()) {
            return;
        }

        gui.setupOverlayRenderState(true, false);
        int x = screenWidth / 2 - 8;
        int y = screenHeight - 52;
        drawDehydrationIcon(graphics, x, y);
    }

    private static void drawDehydrationIcon(GuiGraphics graphics, int x, int y) {
        int outline = 0xCC4C2E16;
        int body = 0xFFE2B16A;
        int shade = 0xFFC58B4C;
        int crack = 0xFF6C3E22;
        int dryHighlight = 0xFFF3D7A0;

        graphics.fill(x + 7, y + 1, x + 9, y + 2, outline);
        graphics.fill(x + 6, y + 2, x + 10, y + 4, outline);
        graphics.fill(x + 5, y + 4, x + 11, y + 7, outline);
        graphics.fill(x + 4, y + 7, x + 12, y + 12, outline);
        graphics.fill(x + 5, y + 12, x + 11, y + 14, outline);
        graphics.fill(x + 6, y + 14, x + 10, y + 15, outline);

        graphics.fill(x + 7, y + 2, x + 9, y + 4, body);
        graphics.fill(x + 6, y + 4, x + 10, y + 7, body);
        graphics.fill(x + 5, y + 7, x + 11, y + 11, body);
        graphics.fill(x + 6, y + 11, x + 10, y + 13, body);

        graphics.fill(x + 7, y + 5, x + 9, y + 10, shade);
        graphics.fill(x + 6, y + 8, x + 8, y + 11, shade);
        graphics.fill(x + 8, y + 8, x + 10, y + 12, shade);

        graphics.fill(x + 7, y + 4, x + 8, y + 5, dryHighlight);
        graphics.fill(x + 7, y + 6, x + 8, y + 7, dryHighlight);
        graphics.fill(x + 6, y + 9, x + 7, y + 10, dryHighlight);

        graphics.fill(x + 7, y + 6, x + 8, y + 12, crack);
        graphics.fill(x + 6, y + 8, x + 10, y + 9, crack);
        graphics.fill(x + 8, y + 3, x + 9, y + 4, crack);
        graphics.fill(x + 5, y + 13, x + 11, y + 14, crack);
    }

    private static boolean isDehydrated() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        return player.getCapability(DehydrationProvider.CAPABILITY)
                .map(cap -> cap.getState().isDehydrated())
                .orElse(false);
    }
}
