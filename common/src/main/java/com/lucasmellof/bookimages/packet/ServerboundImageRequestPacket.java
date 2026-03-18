package com.lucasmellof.bookimages.packet;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.image.ServerImageUploadManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record ServerboundImageRequestPacket(UUID imageId)
        implements CustomPacketPayload {

    public static final Type<ServerboundImageRequestPacket> TYPE =
            new Type<>(Const.of("image_request_packet"));

	public static final StreamCodec<ByteBuf, ServerboundImageRequestPacket> CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ServerboundImageRequestPacket::imageId,
			ServerboundImageRequestPacket::new
	);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

	public void handle(Player player) {
		ServerImageUploadManager.INSTANCE.handleRequest((ServerPlayer) player, this);
	}
}
