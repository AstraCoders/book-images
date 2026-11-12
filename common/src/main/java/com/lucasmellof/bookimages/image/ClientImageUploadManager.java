package com.lucasmellof.bookimages.image;

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.client.widget.BookImageWidget;
import com.lucasmellof.bookimages.duck.IScreen;
import com.lucasmellof.bookimages.packet.*;
import com.lucasmellof.bookimages.platform.Services;
import com.lucasmellof.bookimages.utils.RenderUtils;
import com.mojang.blaze3d.platform.NativeImage;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
@Slf4j
public class ClientImageUploadManager {
    public static final ClientImageUploadManager INSTANCE = new ClientImageUploadManager();

    public static final int DELAY = 1;
    public static final Map<UUID, Identifier> CACHED_IMAGES = new HashMap<>();
    public static final Map<UUID, Long> IN_PROGRESS_IMAGES = new ConcurrentHashMap<>();
    private static final long IMAGE_REQUEST_TIMEOUT_MS = 15_000L;
    private static final long UPLOAD_CONFIRM_TIMEOUT_MS = 15_000L;
    private final Map<UUID, OutgoingImageUpload> ongoingUploads = new ConcurrentHashMap<>();
    private final Map<UUID, IncomingImageUpload> incomingDownloads = new ConcurrentHashMap<>();
    private final Map<UUID, UploadStatus> uploadStatuses = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> failedImageRequests = new ConcurrentHashMap<>();

    private int tickCounter = 0;

    private static byte[] toPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean success = ImageIO.write(image, "png", outputStream);

        if (!success) {
            throw new IOException("Failed to write image to PNG format");
        }
        return outputStream.toByteArray();
    }

    private static BufferedImage fromBytes(byte[] bytes) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new IOException("Failed to read image from bytes");
        }
        return image;
    }

    public static NativeImage toNativeImage(BufferedImage bufferedImage) {
        NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                int rgba = bufferedImage.getRGB(x, y);
                int alpha = RenderUtils.getAlpha(rgba);
                int red = RenderUtils.getRed(rgba);
                int green = RenderUtils.getGreen(rgba);
                int blue = RenderUtils.getBlue(rgba);
                nativeImage.setPixelABGR(x, y, RenderUtils.getArgb(alpha, blue, green, red));
            }
        }
        return nativeImage;
    }

    public void tick() {
        long now = System.currentTimeMillis();
        uploadStatuses.replaceAll((id, status) -> {
            if (status.phase() == UploadPhase.AWAITING_CONFIRMATION
                    && now - status.lastUpdatedAt() >= UPLOAD_CONFIRM_TIMEOUT_MS) {
                return new UploadStatus(UploadPhase.FAILED, status.sentChunks(), status.totalChunks(), now);
            }
            return status;
        });

        if (ongoingUploads.isEmpty() || this.tickCounter-- > 0) return;

        for (OutgoingImageUpload value : ongoingUploads.values()) {
            continueUpload(value.getTransferId());
        }
    }

    public UUID sendImage(@NotNull BufferedImage image) {
        if (Minecraft.getInstance().getConnection() == null) return null;
        try {
            var bytes = toPngBytes(image);

            if (bytes.length == 0 || bytes.length > ServerImageUploadManager.MAX_SIZE) {
                return null;
            }

            var totalChunks = (bytes.length + ServerImageUploadManager.MAX_CHUNK_SIZE - 1)
                    / ServerImageUploadManager.MAX_CHUNK_SIZE;
            var uuid = UUID.randomUUID();
            var upload = new OutgoingImageUpload(
                    uuid, "image_" + uuid, new ByteArrayInputStream(bytes), bytes.length, totalChunks);

            this.ongoingUploads.put(uuid, upload);
            this.uploadStatuses.put(
                    uuid, new UploadStatus(UploadPhase.UPLOADING, 0, totalChunks, System.currentTimeMillis()));
            this.loadImage(uuid, image);

            if (Minecraft.getInstance().screen instanceof IScreen sc && sc.bookImages$isEditable()) {
                sc.bookImages$addComponent(new BookImageWidget(
                        0,
                        0,
                        image.getWidth(),
                        image.getHeight(),
                        image.getWidth(),
                        image.getHeight(),
                        sc,
                        uuid,
                        sc.bookImages$getCurrentPage()));
            }

            Services.PLATFORM.sendPacketToServer(new ServerboundImageUploadStartPacket(
                    upload.getTransferId(), (int) upload.getSize(), upload.getTotalChunks()));
            return uuid;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void continueUpload(UUID transferId) {
        var upload = ongoingUploads.get(transferId);
        if (upload == null) return;
        if (Minecraft.getInstance().getConnection() == null) {
            failUpload(transferId);
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
                failUpload(transferId);
                return;
            }

            var exactData = buf;
            if (read < buf.length) {
                exactData = new byte[read];
                System.arraycopy(buf, 0, exactData, 0, read);
            }

            Services.PLATFORM.sendPacketToServer(new ServerboundImageUploadChunkPacket(
                    transferId, upload.getNextChunkIdx(), upload.getTotalChunks(), exactData));

            upload.incrementNextChunkIdx();
            this.uploadStatuses.put(
                    transferId,
                    new UploadStatus(
                            UploadPhase.UPLOADING,
                            upload.getNextChunkIdx(),
                            upload.getTotalChunks(),
                            System.currentTimeMillis()));
            if (upload.getNextChunkIdx() >= upload.getTotalChunks()) {
                finishUpload(transferId);
            }
        } catch (IOException e) {
            e.printStackTrace();
            failUpload(transferId);
        }
    }

    private void failUpload(UUID transferId) {
        closeAndRemove(transferId);
        UploadStatus status = this.uploadStatuses.get(transferId);
        if (status != null) {
            this.uploadStatuses.put(
                    transferId,
                    new UploadStatus(
                            UploadPhase.FAILED, status.sentChunks(), status.totalChunks(), System.currentTimeMillis()));
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
        Services.PLATFORM.sendPacketToServer(new ServerboundImageUploadFinishPacket(transferId));
        this.uploadStatuses.put(
                transferId,
                new UploadStatus(
                        UploadPhase.AWAITING_CONFIRMATION,
                        upload.getTotalChunks(),
                        upload.getTotalChunks(),
                        System.currentTimeMillis()));
        closeAndRemove(transferId);
    }

    public void loadImage(UUID uuid, BufferedImage image) {
        IN_PROGRESS_IMAGES.remove(uuid);
        failedImageRequests.remove(uuid);

        var id = Const.of("images/book/" + uuid);
        Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .textureManager
                .register(id, new DynamicTexture(id::toString, toNativeImage(image)));
        CACHED_IMAGES.put(uuid, id);
    }

    public Identifier requestImage(UUID uuid) {
        var cached = CACHED_IMAGES.get(uuid);
        if (cached == null) {
            if (failedImageRequests.containsKey(uuid)) {
                return null;
            }

            var inProgress = IN_PROGRESS_IMAGES.get(uuid);
            if (inProgress != null) {
                if (System.currentTimeMillis() - inProgress >= IMAGE_REQUEST_TIMEOUT_MS) {
                    IN_PROGRESS_IMAGES.remove(uuid);
                    failedImageRequests.put(uuid, Boolean.TRUE);
                }
                return null;
            }

            IN_PROGRESS_IMAGES.put(uuid, System.currentTimeMillis());
            Services.PLATFORM.sendPacketToServer(new ServerboundImageRequestPacket(uuid));
            return null;
        }

        return cached;
    }

    public ImageLoadState getImageLoadState(UUID uuid) {
        if (CACHED_IMAGES.containsKey(uuid)) return ImageLoadState.READY;
        if (failedImageRequests.containsKey(uuid)) return ImageLoadState.FAILED;
        return ImageLoadState.LOADING;
    }

    public void retryImage(UUID uuid) {
        failedImageRequests.remove(uuid);
        IN_PROGRESS_IMAGES.remove(uuid);
    }

    public UploadStatus getUploadStatus(UUID uuid) {
        return this.uploadStatuses.get(uuid);
    }

    public void cleanup() {
        CACHED_IMAGES.values().forEach(i -> Minecraft.getInstance()
                                                    .getEntityRenderDispatcher()
                                                    .textureManager.release(i));

        CACHED_IMAGES.clear(); // check if we need to unregister images
        ongoingUploads.clear();
        incomingDownloads.clear();
        uploadStatuses.clear();
        failedImageRequests.clear();
        IN_PROGRESS_IMAGES.clear();
        log.info("Cleaning up");
    }

    public void handleImageStart(Player player, ClientboundImageResponseStartPacket pkt) {
        if (pkt.size() <= 0 || pkt.size() > ServerImageUploadManager.MAX_SIZE) {
            failImageRequest(pkt.imageId());
            return;
        }
        if (pkt.totalChunks() <= 0 || pkt.totalChunks() > ServerImageUploadManager.MAX_CHUNKS) {
            failImageRequest(pkt.imageId());
            return;
        }

        IN_PROGRESS_IMAGES.put(pkt.imageId(), System.currentTimeMillis());
        failedImageRequests.remove(pkt.imageId());
        incomingDownloads.put(
                pkt.imageId(), new IncomingImageUpload("image_" + pkt.imageId(), pkt.size(), pkt.totalChunks()));
    }

    public void handleImageChunk(Player player, ClientboundImageResponseChunkPacket pkt) {
        var upload = incomingDownloads.get(pkt.imageId());
        if (upload == null) {
            return;
        }

        if (pkt.totalChunks() != upload.getTotalChunks()) {
            incomingDownloads.remove(pkt.imageId());
            failImageRequest(pkt.imageId());
            return;
        }

        if (pkt.chunkIndex() < 0 || pkt.chunkIndex() >= upload.getTotalChunks()) {
            incomingDownloads.remove(pkt.imageId());
            failImageRequest(pkt.imageId());
            return;
        }

        long size = upload.getReceivedSize() + pkt.data().length;
        if (size > upload.getExpectedSize() || size > ServerImageUploadManager.MAX_SIZE) {
            incomingDownloads.remove(pkt.imageId());
            failImageRequest(pkt.imageId());
            return;
        }

        boolean accept = upload.receiveChunk(pkt.chunkIndex(), pkt.data());
        if (!accept) {
            incomingDownloads.remove(pkt.imageId());
            failImageRequest(pkt.imageId());
        }
    }

    public void handleImageFinish(Player player, ClientboundImageResponseFinishPacket pkt) {
        var upload = incomingDownloads.remove(pkt.imageId());
        if (upload == null) {
            failImageRequest(pkt.imageId());
            return;
        }

        try {
            upload.finish();
            var image = fromBytes(upload.join());
            loadImage(pkt.imageId(), image);
        } catch (Exception ignored) {
            failImageRequest(pkt.imageId());
        }
    }

    // Legacy fallback for single-packet responses.
    public void handleImage(Player player, ClientboundImageResponsePacket pkt) {
        try {
            var image = fromBytes(pkt.data());
            loadImage(pkt.imageId(), image);
        } catch (Exception ignored) {
            failImageRequest(pkt.imageId());
        }
    }

    public void handleImageConfirm(Player player, ClientboundImageUploadConfirmPacket pkt) {
        UploadStatus pending = this.uploadStatuses.remove(pkt.imageId());
        if (pending == null && Minecraft.getInstance().screen instanceof IScreen sc) {
            var widget = new BookImageWidget(
                    0,
                    0,
                    pkt.width(),
                    pkt.height(),
                    pkt.originalWidth(),
                    pkt.originalHeight(),
                    sc,
                    pkt.imageId(),
                    sc.bookImages$getCurrentPage());
            sc.bookImages$addComponent(widget);
        }
    }

    private void failImageRequest(UUID imageId) {
        IN_PROGRESS_IMAGES.remove(imageId);
        failedImageRequests.put(imageId, Boolean.TRUE);
    }
}
