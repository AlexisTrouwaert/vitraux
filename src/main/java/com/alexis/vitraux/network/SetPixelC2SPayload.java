package com.alexis.vitraux.network;

import com.alexis.vitraux.VitrauxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetPixelC2SPayload(int syncId, int pixelX, int pixelY, byte colorIndex) implements CustomPayload {

    public static final CustomPayload.Id<SetPixelC2SPayload> ID =
        new CustomPayload.Id<>(Identifier.of(VitrauxMod.MOD_ID, "set_pixel"));

    public static final PacketCodec<PacketByteBuf, SetPixelC2SPayload> CODEC = new PacketCodec<>() {
        @Override
        public SetPixelC2SPayload decode(PacketByteBuf buf) {
            return new SetPixelC2SPayload(buf.readVarInt(), buf.readShort(), buf.readShort(), buf.readByte());
        }
        @Override
        public void encode(PacketByteBuf buf, SetPixelC2SPayload value) {
            buf.writeVarInt(value.syncId());
            buf.writeShort(value.pixelX());
            buf.writeShort(value.pixelY());
            buf.writeByte(value.colorIndex());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
