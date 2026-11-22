package com.lucasmellof.bookimages.packet;

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.platform.Services;
import com.lucasmellof.bookimages.utils.BookImagesCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;

public record ServerboundBookImagesSavePacket(boolean mainHand, ListTag images) implements CustomPacketPayload {
    public static final Type<ServerboundBookImagesSavePacket> TYPE = new Type<>(Const.of("book_images_save_packet"));

    public static final StreamCodec<ByteBuf, ServerboundBookImagesSavePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundBookImagesSavePacket::mainHand,
            BookImagesCodecs.LIST_TAG_STREAM_CODEC, ServerboundBookImagesSavePacket::images,
            ServerboundBookImagesSavePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        var hand = this.mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        player.getItemInHand(hand).set(Services.PLATFORM.getBookImagesTag(), this.images.copy());
    }
}
