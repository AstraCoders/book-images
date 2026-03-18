package com.lucasmellof.bookimages.packet;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */

import com.lucasmellof.bookimages.Const;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record ServerboundImageUploadStartPacket(UUID transferId, int size, int totalChunks)
        implements CustomPacketPayload {

    public static final Type<ServerboundImageUploadStartPacket> TYPE =
            new Type<>(Const.of("image_upload_start_packet"));

	public static final StreamCodec<ByteBuf, ServerboundImageUploadStartPacket> CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ServerboundImageUploadStartPacket::transferId,
			ByteBufCodecs.INT, ServerboundImageUploadStartPacket::size,
			ByteBufCodecs.INT, ServerboundImageUploadStartPacket::totalChunks,
			ServerboundImageUploadStartPacket::new
	);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

	public void handle() {

	}
}
