package com.lucasmellof.bookimages.mixin;

import com.lucasmellof.bookimages.client.ClientStuff;
import com.lucasmellof.bookimages.duck.IScreen;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import net.minecraft.client.gui.components.AbstractWidget;
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
public class MixinBookEditScreen extends Screen implements IScreen {
    protected MixinBookEditScreen(Component title) {
        super(title);
        throw new RuntimeException("MixinBookEditScreen constructor should not be called");
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void bookImages$keyPressed(KeyEvent ev, CallbackInfoReturnable<Boolean> cir) {
        if (!ev.isPaste()) return;
        try {
            var image = ClientStuff.pasteImage();
			if (image == null) return;
            ClientImageUploadManager.INSTANCE.sendImage(image);
        	cir.setReturnValue(true);
		} catch (Exception ignored) {
        }
    }

	@Override
	public void bookImages$addComponent(AbstractWidget widget) {
		this.addRenderableWidget(widget);
	}
}
