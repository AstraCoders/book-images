package com.lucasmellof.bookimages;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
public class ModRegistry {
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

    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Const.MOD_ID);
    public static final Supplier<DataComponentType<ListTag>> BOOK_IMAGES_TAG = DATA_COMPONENTS.registerComponentType(
            "book_images",
            (builder) -> builder.networkSynchronized(LIST_TAG_STREAM_CODEC).persistent(LIST_TAG_CODEC));


	public static void register(IEventBus bus) {
		DATA_COMPONENTS.register(bus);
	}
}
