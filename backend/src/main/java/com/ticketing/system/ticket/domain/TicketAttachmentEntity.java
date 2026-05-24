package com.ticketing.system.ticket.domain;

import com.ticketing.system.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "ticket_attachments")
public class TicketAttachmentEntity extends BaseEntity {

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "storage_key", nullable = false, unique = true, length = 500)
    private String storageKey;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    protected TicketAttachmentEntity() {
    }

    public TicketAttachmentEntity(UUID ticketId, UUID uploadedBy, String fileName, String contentType, long fileSizeBytes, String storageKey, String checksumSha256) {
        this.ticketId = ticketId;
        this.uploadedBy = uploadedBy;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.storageKey = storageKey;
        this.checksumSha256 = checksumSha256;
    }

    public UUID getTicketId() { return ticketId; }
    public UUID getUploadedBy() { return uploadedBy; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public String getStorageKey() { return storageKey; }
    public String getChecksumSha256() { return checksumSha256; }
}
