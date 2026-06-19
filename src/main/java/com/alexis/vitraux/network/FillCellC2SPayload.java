package com.alexis.vitraux.network;

import com.alexis.vitraux.VitrauxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FillCellC2SPayload(int syncId, int cellCol, int cellRow, byte colorIndex) implements CustomPayload {

    public static final CustomPayload.Id<FillCellC2SPayload> ID =
        new CustomPayload.Id<>(Identifier.of(VitrauxMod.MOD_ID, "fill_cell"));

    public static final PacketCodec<PacketByteBuf, FillCellC2SPayload> CODEC = new PacketCodec<>() {
        @Override
        public FillCellC2SPayload decode(PacketByteBuf buf) {
            return new FillCellC2SPayload(buf.readVarInt(), buf.readByte(), buf.readByte(), buf.readByte());
        }
        @Override
        public void encode(PacketByteBuf buf, FillCellC2SPayload value) {
            buf.writeVarInt(value.syncId());
            buf.writeByte(value.cellCol());
            buf.writeByte(value.cellRow());
            buf.writeByte(value.colorIndex());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
