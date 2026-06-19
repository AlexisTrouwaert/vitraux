package com.alexis.vitraux.network;

import com.alexis.vitraux.VitrauxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FillRectC2SPayload(int syncId, int gx0, int gy0, int gx1, int gy1, byte colorIndex)
        implements CustomPayload {

    public static final CustomPayload.Id<FillRectC2SPayload> ID =
            new CustomPayload.Id<>(Identifier.of(VitrauxMod.MOD_ID, "fill_rect"));

    public static final PacketCodec<PacketByteBuf, FillRectC2SPayload> CODEC = new PacketCodec<>() {
        @Override
        public FillRectC2SPayload decode(PacketByteBuf buf) {
            return new FillRectC2SPayload(
                buf.readVarInt(),
                buf.readShort(), buf.readShort(),
                buf.readShort(), buf.readShort(),
                buf.readByte());
        }
        @Override
        public void encode(PacketByteBuf buf, FillRectC2SPayload v) {
            buf.writeVarInt(v.syncId());
            buf.writeShort(v.gx0()); buf.writeShort(v.gy0());
            buf.writeShort(v.gx1()); buf.writeShort(v.gy1());
            buf.writeByte(v.colorIndex());
        }
    };

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
