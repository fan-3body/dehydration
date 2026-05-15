package org.fan_3body.dehydration.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.fan_3body.dehydration.client.ClientDehydrationSync;
import org.fan_3body.dehydration.dehydration.DehydrationStage;

import java.util.function.Supplier;

public final class SyncDehydrationPacket {
    private final DehydrationStage state;
    private final boolean firstDehydration;
    private final boolean firstRehydration;

    public SyncDehydrationPacket(DehydrationStage state, boolean firstDehydration, boolean firstRehydration) {
        this.state = state;
        this.firstDehydration = firstDehydration;
        this.firstRehydration = firstRehydration;
    }

    public DehydrationStage getState() {
        return state;
    }

    public boolean isFirstDehydration() {
        return firstDehydration;
    }

    public boolean isFirstRehydration() {
        return firstRehydration;
    }

    public static void encode(SyncDehydrationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.state);
        buffer.writeBoolean(packet.firstDehydration);
        buffer.writeBoolean(packet.firstRehydration);
    }

    public static SyncDehydrationPacket decode(FriendlyByteBuf buffer) {
        return new SyncDehydrationPacket(
                buffer.readEnum(DehydrationStage.class),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    public static void handle(SyncDehydrationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientDehydrationSync.apply(packet.getState(), packet.isFirstDehydration(), packet.isFirstRehydration())));
        context.setPacketHandled(true);
    }
}
