package com.lucasmellof.bookimages.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
public class CodecUtils {
	// https://github.com/Creators-of-Create/Ponder/blob/bc68611f2c0172c7885645c32017f92209c700eb/common/src/main/java/net/createmod/catnip/codecs/stream/CatnipStreamCodecBuilders.java#L57-L70
	static <B extends ByteBuf, V> StreamCodec<B, V> nullable(StreamCodec<B, V> base) {
		return new StreamCodec<>() {
			@Override
			@SuppressWarnings("NullableProblems")
			public @Nullable V decode(@NotNull B buffer) {
				return FriendlyByteBuf.readNullable(buffer, base);
			}

			@Override
			public void encode(@NotNull B buffer, @Nullable V value) {
				FriendlyByteBuf.writeNullable(buffer, value, base);
			}
		};
	}
}
