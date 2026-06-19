package com.alexis.vitraux.network;

import com.alexis.vitraux.VitrauxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CreateTemplateC2SPayload(int syncId) implements CustomPayload {

    public static final CustomPayload.Id<CreateTemplateC2SPayload> ID =
        new CustomPayload.Id<>(Identifier.of(VitrauxMod.MOD_ID, "create_template"));

    public static final PacketCodec<PacketByteBuf, CreateTemplateC2SPayload> CODEC = new PacketCodec<>() {
        @Override
        public CreateTemplateC2SPayload decode(PacketByteBuf buf) {
            return new CreateTemplateC2SPayload(buf.readVarInt());
        }
        @Override
        public void encode(PacketByteBuf buf, CreateTemplateC2SPayload value) {
            buf.writeVarInt(value.syncId());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
