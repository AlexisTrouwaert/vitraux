package com.alexis.vitraux.network;

import com.alexis.vitraux.VitrauxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetDimensionsC2SPayload(int syncId, int width, int height) implements CustomPayload {

    public static final CustomPayload.Id<SetDimensionsC2SPayload> ID =
        new CustomPayload.Id<>(Identifier.of(VitrauxMod.MOD_ID, "set_dimensions"));

    public static final PacketCodec<PacketByteBuf, SetDimensionsC2SPayload> CODEC = new PacketCodec<>() {
        @Override
        public SetDimensionsC2SPayload decode(PacketByteBuf buf) {
            return new SetDimensionsC2SPayload(buf.readVarInt(), buf.readByte(), buf.readByte());
        }
        @Override
        public void encode(PacketByteBuf buf, SetDimensionsC2SPayload value) {
            buf.writeVarInt(value.syncId());
            buf.writeByte(value.width());
            buf.writeByte(value.height());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
