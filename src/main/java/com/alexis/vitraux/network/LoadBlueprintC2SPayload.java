package com.alexis.vitraux.network;

import com.alexis.vitraux.VitrauxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LoadBlueprintC2SPayload(int syncId) implements CustomPayload {

    public static final CustomPayload.Id<LoadBlueprintC2SPayload> ID =
        new CustomPayload.Id<>(Identifier.of(VitrauxMod.MOD_ID, "load_blueprint"));

    public static final PacketCodec<PacketByteBuf, LoadBlueprintC2SPayload> CODEC = new PacketCodec<>() {
        @Override
        public LoadBlueprintC2SPayload decode(PacketByteBuf buf) {
            return new LoadBlueprintC2SPayload(buf.readVarInt());
        }
        @Override
        public void encode(PacketByteBuf buf, LoadBlueprintC2SPayload value) {
            buf.writeVarInt(value.syncId());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
