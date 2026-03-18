package com.lucasmellof.bookimages.packet;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record ClientboundImageResponseStartPacket(UUID imageId, int size, int totalChunks)
        implements CustomPacketPayload {

    public static final Type<ClientboundImageResponseStartPacket> TYPE =
            new Type<>(Const.of("image_response_start_packet"));

    public static final StreamCodec<ByteBuf, ClientboundImageResponseStartPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundImageResponseStartPacket::imageId,
            ByteBufCodecs.INT, ClientboundImageResponseStartPacket::size,
            ByteBufCodecs.INT, ClientboundImageResponseStartPacket::totalChunks,
            ClientboundImageResponseStartPacket::new
    );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(Player player) {
        ClientImageUploadManager.INSTANCE.handleImageStart(player, this);
    }
}

