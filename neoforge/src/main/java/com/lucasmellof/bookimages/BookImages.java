package com.lucasmellof.bookimages;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Const.MOD_ID)
public class BookImages {

    public BookImages(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Const.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        ModRegistry.register(eventBus);

    }
}
