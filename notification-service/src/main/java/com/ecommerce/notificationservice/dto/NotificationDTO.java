
package com.ecommerce.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String to;
    private String subject;
    private String body;
    private NotificationType type;

    public NotificationDTO(String customerEmail, String orderCreated, String s) {
    }

    public enum NotificationType {
        EMAIL, SMS, PUSH
    }
}
