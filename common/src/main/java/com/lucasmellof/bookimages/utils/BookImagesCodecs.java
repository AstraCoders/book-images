package com.lucasmellof.bookimages.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class BookImagesCodecs {

    // https://github.com/Fuzss/puzzleslib/blob/8f6970cf8839970878f55adb6c32cf25d75ba759/1.21.5/Common/src/main/java/fuzs/puzzleslib/api/util/v1/CodecExtras.java#L36
    public static final Codec<ListTag> LIST_TAG_CODEC = Codec.PASSTHROUGH.comapFlatMap(
            (Dynamic<?> dynamic) -> {
                Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
                return tag instanceof ListTag listTag
                        ? DataResult.success(listTag == dynamic.getValue() ? listTag.copy() : listTag)
                        : DataResult.error(() -> "Not a list tag: " + tag);
            },
            (ListTag listTag) -> new Dynamic<>(NbtOps.INSTANCE, listTag.copy()));

    public static final StreamCodec<ByteBuf, ListTag> LIST_TAG_STREAM_CODEC = ByteBufCodecs.fromCodec(LIST_TAG_CODEC);
}
