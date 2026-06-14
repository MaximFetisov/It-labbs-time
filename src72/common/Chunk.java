package common;

import java.io.Serializable;

/**
 * Класс, представляющий один чанк данных для передачи по сети.
 */
public class Chunk implements Serializable {
    private static final long serialVersionUID = 1L;

    private int messageId;        // Уникальный ID сообщения
    private int chunkIndex;       // Номер этого чанка (0, 1, 2...)
    private int totalChunks;      // Всего чанков в сообщении
    private byte[] data;          // Данные чанка
    private boolean isLast;       // Последний ли это чанк

    public Chunk(int messageId, int chunkIndex, int totalChunks, byte[] data, boolean isLast) {
        this.messageId = messageId;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
        this.data = data;
        this.isLast = isLast;
    }

    // Геттеры
    public int getMessageId() { return messageId; }
    public int getChunkIndex() { return chunkIndex; }
    public int getTotalChunks() { return totalChunks; }
    public byte[] getData() { return data; }
    public boolean isLast() { return isLast; }
}