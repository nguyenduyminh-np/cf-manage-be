package com.duyminhdev.cf_manager.dto.response.notification;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponseDTO {
    private Integer id;
    private String title;
    private String description;
    private String url;
    private Instant createdAt;
    private boolean read;
    /** Tên event WS nguồn gốc, ví dụ "TABLE_OCCUPIED_CONFLICT" */
    private String eventType;
    /** Topic WS nguồn gốc, ví dụ "/topic/table-alerts" */
    private String topic;
}
