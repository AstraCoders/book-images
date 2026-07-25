package com.lucasmellof.bookimages;

import com.lucasmellof.bookimages.events.ForgeClientEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(Const.MOD_ID)
public class BookImages {
    public BookImages(FMLJavaModLoadingContext context) {
        ModRegistry.register(context.getModBusGroup());
        ForgeNetwork.register();
        CommonClass.init();

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ForgeClientEvents.register();
        }
    }
}
