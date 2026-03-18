package com.lucasmellof.bookimages.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
@Mixin(Main.class)
public class MixinMain {
	@Inject(method = "main", at = @At("HEAD"), cancellable = true)
	private static void bookIamges$onInit(String[] args, CallbackInfo ci) {
		if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")) {
			System.out.println("[BookImages] Setting java.awt.headless to false");
			System.setProperty("java.awt.headless", "false");
		}
	}
}
