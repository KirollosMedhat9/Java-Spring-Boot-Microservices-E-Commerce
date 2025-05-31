
package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.dto.NotificationDTO;
import com.ecommerce.notificationservice.dto.OrderCreatedEvent;

public interface NotificationService {
    void sendNotification(NotificationDTO notificationDTO);
    void sendOrderConfirmation(OrderCreatedEvent orderEvent);
    void sendWelcomeEmail(String email, String customerName);
    void sendOrderStatusUpdate(String email, Long orderId, String status);
}
