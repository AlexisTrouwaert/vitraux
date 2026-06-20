package com.alexis.vitraux.network;

import com.alexis.vitraux.VitrauxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SaveDesignC2SPayload(int syncId, String name) implements CustomPayload {

    public static final int MAX_NAME_LENGTH = 32;

    public static final CustomPayload.Id<SaveDesignC2SPayload> ID =
        new CustomPayload.Id<>(Identifier.of(VitrauxMod.MOD_ID, "save_design"));

    public static final PacketCodec<PacketByteBuf, SaveDesignC2SPayload> CODEC = new PacketCodec<>() {
        @Override
        public SaveDesignC2SPayload decode(PacketByteBuf buf) {
            return new SaveDesignC2SPayload(buf.readVarInt(), buf.readString(MAX_NAME_LENGTH));
        }
        @Override
        public void encode(PacketByteBuf buf, SaveDesignC2SPayload value) {
            buf.writeVarInt(value.syncId());
            buf.writeString(value.name(), MAX_NAME_LENGTH);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
