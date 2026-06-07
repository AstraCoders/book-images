package com.lucasmellof.bookimages.mixin;

import com.lucasmellof.bookimages.duck.IBookAccess;
import com.lucasmellof.bookimages.platform.Services;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
@Mixin(BookViewScreen.BookAccess.class)
public class MixinBookAccess implements IBookAccess {

	@Unique
	private ListTag bookImages$images;

	@Override
	public ListTag bookImages$getImages() {
		return this.bookImages$images;
	}

	@Override
	public void bookImages$setImages(ListTag tag) {
		this.bookImages$images = tag;
	}

	@Inject(method = "fromItem", at = @At("RETURN"))
	private static void bookImages$fromItem(ItemStack stack, CallbackInfoReturnable<BookViewScreen.BookAccess> cir) {
		var access = cir.getReturnValue();
		if (access == null) return;
		IBookAccess.of(access).bookImages$setImages(stack.get(Services.PLATFORM.getBookImagesTag()));
	}
}
