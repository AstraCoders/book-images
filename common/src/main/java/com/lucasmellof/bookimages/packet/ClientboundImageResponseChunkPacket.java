package com.lucasmellof.bookimages.packet;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import com.lucasmellof.bookimages.image.ServerImageUploadManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record ClientboundImageResponseChunkPacket(UUID imageId, int chunkIndex, int totalChunks, byte[] data)
        implements CustomPacketPayload {

    public static final Type<ClientboundImageResponseChunkPacket> TYPE =
            new Type<>(Const.of("image_response_chunk_packet"));

    public static final StreamCodec<ByteBuf, ClientboundImageResponseChunkPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundImageResponseChunkPacket::imageId,
            ByteBufCodecs.INT, ClientboundImageResponseChunkPacket::chunkIndex,
            ByteBufCodecs.INT, ClientboundImageResponseChunkPacket::totalChunks,
            ByteBufCodecs.byteArray(ServerImageUploadManager.MAX_CHUNK_SIZE), ClientboundImageResponseChunkPacket::data,
            ClientboundImageResponseChunkPacket::new
    );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(Player player) {
        ClientImageUploadManager.INSTANCE.handleImageChunk(player, this);
    }
}

