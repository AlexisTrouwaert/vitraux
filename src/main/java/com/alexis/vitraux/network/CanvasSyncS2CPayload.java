package com.alexis.vitraux.network;

import com.alexis.vitraux.VitrauxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CanvasSyncS2CPayload(int syncId, int width, int height, byte[] pixels) implements CustomPayload {

    public static final CustomPayload.Id<CanvasSyncS2CPayload> ID =
        new CustomPayload.Id<>(Identifier.of(VitrauxMod.MOD_ID, "canvas_sync"));

    public static final PacketCodec<PacketByteBuf, CanvasSyncS2CPayload> CODEC = new PacketCodec<>() {
        @Override
        public CanvasSyncS2CPayload decode(PacketByteBuf buf) {
            int syncId = buf.readVarInt();
            int width  = buf.readByte();
            int height = buf.readByte();
            byte[] pixels = new byte[width * 16 * height * 16];
            buf.readBytes(pixels);
            return new CanvasSyncS2CPayload(syncId, width, height, pixels);
        }
        @Override
        public void encode(PacketByteBuf buf, CanvasSyncS2CPayload value) {
            buf.writeVarInt(value.syncId());
            buf.writeByte(value.width());
            buf.writeByte(value.height());
            buf.writeBytes(value.pixels());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
