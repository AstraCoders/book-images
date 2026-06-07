package com.lucasmellof.bookimages;

import com.lucasmellof.bookimages.utils.BookImagesCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
public class ModRegistry {
    public static final StreamCodec<ByteBuf, ListTag> LIST_TAG_STREAM_CODEC = BookImagesCodecs.LIST_TAG_STREAM_CODEC;

    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Const.MOD_ID);
    public static final Supplier<DataComponentType<ListTag>> BOOK_IMAGES_TAG = DATA_COMPONENTS.registerComponentType(
            "book_images",
            (builder) -> builder.networkSynchronized(LIST_TAG_STREAM_CODEC).persistent(BookImagesCodecs.LIST_TAG_CODEC));


	public static void register(IEventBus bus) {
		DATA_COMPONENTS.register(bus);
	}
}
