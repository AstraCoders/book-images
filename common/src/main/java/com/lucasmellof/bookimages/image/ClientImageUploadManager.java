package com.lucasmellof.bookimages.image;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
public class ClientImageUploadManager {

    public static final int DELAY = 2;

    private final Map<UUID, OutgoingImageUpload> ongoingUploads = new HashMap<>();

    private int tickCounter = 0;

    public void tick() {
        if (ongoingUploads.isEmpty()) return;
        if (this.tickCounter-- > 0) return;

        for (OutgoingImageUpload value : ongoingUploads.values()) {
            continueUpload(value.getTransferId());
        }
    }

    public void sendImage(@NotNull BufferedImage image) {
        if (Minecraft.getInstance().getConnection() == null) return;
        try{
            var bytes = toPngBytes(image);

            if (bytes.length == 0 || bytes.length > ServerImageUploadManager.MAX_SIZE) {
                return;
            }

            var totalChunks = (bytes.length + ServerImageUploadManager.MAX_CHUNK_SIZE - 1) / ServerImageUploadManager.MAX_CHUNK_SIZE;
            var uuid = UUID.randomUUID();
            var upload = new OutgoingImageUpload("image_" + uuid, new ByteArrayInputStream(bytes), bytes.length, totalChunks);

            this.ongoingUploads.put(uuid, upload);

            // send start packet
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void continueUpload(UUID transferId) {
        var upload = ongoingUploads.get(transferId);
        if (upload == null) return;
        if (Minecraft.getInstance().getConnection() == null) {
            closeAndRemove(transferId);
            return;
        }
        if (upload.getNextChunkIdx() >= upload.getTotalChunks()) {
            finishUpload(transferId);
            return;
        }

        var buf = new byte[ServerImageUploadManager.MAX_CHUNK_SIZE];
        try {
            var read = upload.getInputStream().read(buf);

            if (read == -1) {
                closeAndRemove(transferId);
                return;
            }

            var exactData = buf;
            if (read < buf.length) {
                exactData = new byte[read];
                System.arraycopy(buf, 0, exactData, 0, read);
            }

            // send packet
            upload.incrementNextChunkIdx();
            if (upload.getNextChunkIdx() >= upload.getTotalChunks()) {
                finishUpload(transferId);
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeAndRemove(transferId);
        }
    }

    private void closeAndRemove(UUID transferId) {
        OutgoingImageUpload upload = this.ongoingUploads.remove(transferId);
        if (upload == null) {
            return;
        }

        try {
            upload.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void finishUpload(UUID transferId) {
        var upload = ongoingUploads.get(transferId);
        if (upload == null) return;
        // send packet
        closeAndRemove(transferId);
    }

    private static byte[] toPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean success = ImageIO.write(image, "png", outputStream);

        if (!success) {
            throw new IOException("Failed to write image to PNG format");
        }
        return outputStream.toByteArray();
    }
}

