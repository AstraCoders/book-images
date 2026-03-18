package com.lucasmellof.bookimages.mixin;

import com.lucasmellof.bookimages.client.ClientStuff;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
@Mixin(BookEditScreen.class)
public class MixinBookEditScreen extends Screen {
	protected MixinBookEditScreen(Component title) {
		super(title);
		throw new RuntimeException("MixinBookEditScreen constructor should not be called");
	}

	@Inject(method = "keyPressed", at = @At("HEAD"))
	public void bookImages$keyPressed(KeyEvent ev, CallbackInfoReturnable<Boolean> cir) {
		if (!ev.isPaste()) return;
		ClientStuff.pasteImage(file -> {

		});
	}
}
