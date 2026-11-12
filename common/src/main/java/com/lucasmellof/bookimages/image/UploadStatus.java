package com.lucasmellof.bookimages.image;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 28/07/2026
 */
public record UploadStatus(UploadPhase phase, int sentChunks, int totalChunks, long lastUpdatedAt) {
    public float progress() {
        return this.totalChunks <= 0 ? 0.0F : Math.clamp((float) this.sentChunks / this.totalChunks, 0.0F, 1.0F);
    }
}
