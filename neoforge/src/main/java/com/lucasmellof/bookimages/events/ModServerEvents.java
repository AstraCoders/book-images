package com.lucasmellof.bookimages.events;

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.packet.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class ModServerEvents {
    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(
                ServerboundImageUploadStartPacket.TYPE,
                ServerboundImageUploadStartPacket.CODEC,
                (pkt, ctx) -> pkt.handle(ctx.player()));
		registrar.playToServer(
				ServerboundImageUploadChunkPacket.TYPE,
				ServerboundImageUploadChunkPacket.CODEC,
				(pkt, ctx) -> pkt.handle(ctx.player()));
		registrar.playToServer(
				ServerboundImageUploadFinishPacket.TYPE,
				ServerboundImageUploadFinishPacket.CODEC,
				(pkt, ctx) -> pkt.handle(ctx.player()));
		registrar.playToServer(
				ServerboundImageRequestPacket.TYPE,
				ServerboundImageRequestPacket.CODEC,
				(pkt, ctx) -> pkt.handle(ctx.player()));
		registrar.playToServer(
				ServerboundBookImagesSavePacket.TYPE,
				ServerboundBookImagesSavePacket.CODEC,
				(pkt, ctx) -> pkt.handle(ctx.player()));

		// server -> client
		registrar.playToClient(
				ClientboundImageResponseStartPacket.TYPE,
				ClientboundImageResponseStartPacket.CODEC,
				(pkt, ctx) -> pkt.handle(ctx.player()));
		registrar.playToClient(
				ClientboundImageResponseChunkPacket.TYPE,
				ClientboundImageResponseChunkPacket.CODEC,
				(pkt, ctx) -> pkt.handle(ctx.player()));
		registrar.playToClient(
				ClientboundImageResponseFinishPacket.TYPE,
				ClientboundImageResponseFinishPacket.CODEC,
				(pkt, ctx) -> pkt.handle(ctx.player()));
		registrar.playToClient(
				ClientboundImageUploadConfirmPacket.TYPE,
				ClientboundImageUploadConfirmPacket.CODEC,
				(pkt, ctx) -> pkt.handle(ctx.player()));
    }
}
