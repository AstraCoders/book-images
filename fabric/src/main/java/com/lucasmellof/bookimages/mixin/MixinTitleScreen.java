package com.lucasmellof.bookimages.mixin;

import com.lucasmellof.bookimages.Const;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {

        Const.LOG.info("This line is printed by an example mod mixin from Fabric!");
        Const.LOG.info("MC Version: {}", SharedConstants.getCurrentVersion().name());
    }
}
