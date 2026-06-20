package com.alexis.vitraux.network;

import com.alexis.vitraux.VitrauxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CreateTemplateC2SPayload(int syncId, String name) implements CustomPayload {

    public static final int MAX_NAME_LENGTH = 32;

    public static final CustomPayload.Id<CreateTemplateC2SPayload> ID =
        new CustomPayload.Id<>(Identifier.of(VitrauxMod.MOD_ID, "create_template"));

    public static final PacketCodec<PacketByteBuf, CreateTemplateC2SPayload> CODEC = new PacketCodec<>() {
        @Override
        public CreateTemplateC2SPayload decode(PacketByteBuf buf) {
            return new CreateTemplateC2SPayload(buf.readVarInt(), buf.readString(MAX_NAME_LENGTH));
        }
        @Override
        public void encode(PacketByteBuf buf, CreateTemplateC2SPayload value) {
            buf.writeVarInt(value.syncId());
            buf.writeString(value.name(), MAX_NAME_LENGTH);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
