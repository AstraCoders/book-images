package com.lucasmellof.bookimages.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.extern.slf4j.Slf4j;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
@Slf4j
@Mixin(RenderSystem.class)
public class MixinRenderSystem {
    @Inject(method = "initRenderThread", at = @At("HEAD"))
    private static void onInit(CallbackInfo ci) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (HeadlessException e) {
            log.warn("java.awt.headless property was not set properly!");
        }
    }
}
