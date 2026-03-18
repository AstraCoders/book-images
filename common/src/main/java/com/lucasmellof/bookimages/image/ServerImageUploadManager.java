package com.lucasmellof.bookimages.image;

import com.lucasmellof.bookimages.packet.ServerboundImageUploadChunkPacket;
import com.lucasmellof.bookimages.packet.ServerboundImageUploadFinishPacket;
import com.lucasmellof.bookimages.packet.ServerboundImageUploadStartPacket;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
@Slf4j
@NoArgsConstructor
public class ServerImageUploadManager {

    public static final int MAX_CHUNK_SIZE = 32_767;  // using safe values from create's schematicanon
    public static final int MAX_CHUNKS = 1024; // 32MB max size
    public static final int MAX_SIZE = MAX_CHUNK_SIZE * MAX_CHUNKS;
    private static final Map<UUID, Map<UUID, IncomingImageUpload>> ACTIVE_UPLOADS = new HashMap<>();

    public void handleStart(ServerPlayer player, ServerboundImageUploadStartPacket pkt) {
        if (pkt.size() <= 0 || pkt.size() > MAX_SIZE) {
            log.warn(
                    "Player {} attempted to start an image upload with invalid size: {}",
                    player.getName().getString(),
                    pkt.size());
            return;
        }
        if (pkt.totalChunks() <= 0 || pkt.totalChunks() > MAX_CHUNKS) {
            log.warn(
                    "Player {} attempted to start an image upload with invalid chunk count: {}",
                    player.getName().getString(),
                    pkt.totalChunks());
            return;
        }

        var playerUploads = ACTIVE_UPLOADS.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        playerUploads.put(
                pkt.transferId(), new IncomingImageUpload("image_" + pkt.transferId(), pkt.size(), pkt.totalChunks()));
    }

    public void handleChunk(ServerPlayer player, ServerboundImageUploadChunkPacket pkt) {
        var playerUploads = ACTIVE_UPLOADS.get(player.getUUID());
        if (playerUploads == null) {
            log.warn(
                    "Received image chunk for player {} but they have no active uploads",
                    player.getName().getString());
            return;
        }

        var upload = playerUploads.get(pkt.transferId());
        if (upload == null) {
            log.warn(
                    "Received image chunk for player {} but no upload found with ID {}",
                    player.getName().getString(),
                    pkt.transferId());
            return;
        }

        if (pkt.totalChunks() != upload.getTotalChunks()) {
            log.warn(
                    "Received image chunk for player {} with mismatched total chunks: expected {} but got {}",
                    player.getName().getString(),
                    upload.getTotalChunks(),
                    pkt.totalChunks());
            clean(playerUploads, pkt.transferId());
            return;
        }

        if (pkt.chunkIndex() < 0 || pkt.chunkIndex() >= upload.getTotalChunks()) {
            log.warn(
                    "Received image chunk for player {} with invalid chunk index: {}",
                    player.getName().getString(),
                    pkt.chunkIndex());
            clean(playerUploads, pkt.transferId());
            return;
        }

        long size = upload.getReceivedSize() + pkt.data().length;
        if (size > upload.getExpectedSize() || size > MAX_SIZE) {
            log.warn(
                    "Received image chunk for player {} that would exceed expected size: current {} + chunk {} >"
                            + " expected {}",
                    player.getName().getString(),
                    upload.getReceivedSize(),
                    pkt.data().length,
                    upload.getExpectedSize());
            clean(playerUploads, pkt.transferId());
            return;
        }

        boolean accept = upload.receiveChunk(pkt.chunkIndex(), pkt.data());
        if (!accept) {
            clean(playerUploads, pkt.transferId());
            return;
        }

        tryComplete(player, playerUploads, pkt.transferId(), upload);
    }

    public void handleFinish(ServerPlayer player, ServerboundImageUploadFinishPacket pkt) {
        Map<UUID, IncomingImageUpload> playerUploads = ACTIVE_UPLOADS.get(player.getUUID());
        if (playerUploads == null) {
            log.warn(
                    "Received image upload finish for player {} but they have no active uploads",
                    player.getName().getString());
            return;
        }

        IncomingImageUpload upload = playerUploads.get(pkt.transferId());
        if (upload == null) {
            log.warn(
                    "Received image upload finish for player {} but no upload found with ID {}",
                    player.getName().getString(),
                    pkt.transferId());
            return;
        }

        upload.finish();
        tryComplete(player, playerUploads, pkt.transferId(), upload);
    }

    private void tryComplete(
            ServerPlayer player,
            Map<UUID, IncomingImageUpload> playerUploads,
            UUID transferId,
            IncomingImageUpload upload) {
        if (!upload.isFinished()) {
            return;
        }

        if (!upload.isComplete()) {
            clean(playerUploads, transferId);
            return;
        }

        byte[] imageData;
        try {
            imageData = upload.join();
        } catch (IllegalStateException e) {
            log.warn(
                    "Failed to join image chunks for player {} upload {}: {}",
                    player.getName().getString(),
                    transferId,
                    e.getMessage());
            clean(playerUploads, transferId);
            return;
        }

        clean(playerUploads, transferId);

        File folder = player.level()
                .getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("book_images")
                .toFile();
        if (!folder.exists() && !folder.mkdirs()) {
            log.warn("Failed to create book_images folder at {}", folder.getAbsolutePath());
            return;
        }
		var imageFile = new File(folder, "image_" + transferId + ".png");
		try {
			Files.write(imageFile.toPath(), imageData);
			log.info("Saved uploaded image for player {} to disk at {}",
					player.getName().getString(),
					imageFile.getAbsolutePath());
		} catch (java.io.IOException e) {
			log.warn("Failed to save uploaded image for player {} to disk: {}",
					player.getName().getString(),
					e.getMessage());
			return;
		}
        // todo: inform client of successful upload and provide image data to book item logic
    }

    private void clean(Map<UUID, IncomingImageUpload> playerUploads, UUID transferId) {
        playerUploads.remove(transferId);
    }
}
