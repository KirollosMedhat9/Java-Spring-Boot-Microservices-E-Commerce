
package com.ecommerce.notificationservice.service.impl;

import com.ecommerce.notificationservice.dto.NotificationDTO;
import com.ecommerce.notificationservice.dto.OrderCreatedEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final JavaMailSender mailSender;
    
    @Override
    public void sendNotification(NotificationDTO notificationDTO) {
        switch (notificationDTO.getType()) {
            case EMAIL:
                sendEmail(notificationDTO.getTo(), notificationDTO.getSubject(), notificationDTO.getBody());
                break;
            case SMS:
                sendSMS(notificationDTO.getTo(), notificationDTO.getBody());
                break;
            case PUSH:
                sendPushNotification(notificationDTO.getTo(), notificationDTO.getSubject(), notificationDTO.getBody());
                break;
            default:
                log.warn("Unknown notification type: {}", notificationDTO.getType());
        }
    }
    
    @Override
    @KafkaListener(topics = "order.created", groupId = "notification-service")
    public void sendOrderConfirmation(OrderCreatedEvent orderEvent) {
        log.info("Sending order confirmation for order: {}", orderEvent.getOrderId());
        
        String subject = "Order Confirmation - Order #" + orderEvent.getOrderId();
        String body = buildOrderConfirmationEmail(orderEvent);
        
        sendEmail(orderEvent.getCustomerEmail(), subject, body);
    }
    
    @Override
    public void sendWelcomeEmail(String email, String customerName) {
        String subject = "Welcome to Our E-commerce Platform!";
        String body = "Dear " + customerName + ",\n\n" +
                     "Welcome to our e-commerce platform! We're excited to have you as a customer.\n\n" +
                     "Start exploring our products and enjoy shopping with us!\n\n" +
                     "Best regards,\nThe E-commerce Team";
        
        sendEmail(email, subject, body);
    }
    
    @Override
    public void sendOrderStatusUpdate(String email, Long orderId, String status) {
        String subject = "Order Status Update - Order #" + orderId;
        String body = "Your order #" + orderId + " status has been updated to: " + status + "\n\n" +
                     "Thank you for shopping with us!\n\n" +
                     "Best regards,\nThe E-commerce Team";
        
        sendEmail(email, subject, body);
    }
    
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@ecommerce.com");
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
    
    private void sendSMS(String phoneNumber, String message) {
        // Implementation for SMS sending (e.g., using Twilio, AWS SNS, etc.)
        log.info("SMS sent to {}: {}", phoneNumber, message);
    }
    
    private void sendPushNotification(String deviceToken, String title, String body) {
        // Implementation for push notifications (e.g., using Firebase Cloud Messaging)
        log.info("Push notification sent to {}: {} - {}", deviceToken, title, body);
    }
    
    private String buildOrderConfirmationEmail(OrderCreatedEvent orderEvent) {
        StringBuilder body = new StringBuilder();
        body.append("Dear Customer,\n\n");
        body.append("Thank you for your order! Here are the details:\n\n");
        body.append("Order ID: ").append(orderEvent.getOrderId()).append("\n");
        body.append("Total Amount: $").append(orderEvent.getTotalAmount()).append("\n\n");
        body.append("Items:\n");
        
        for (OrderCreatedEvent.OrderItemEvent item : orderEvent.getItems()) {
            body.append("- Product ID: ").append(item.getProductId())
                .append(", Quantity: ").append(item.getQuantity())
                .append(", Price: $").append(item.getPrice()).append("\n");
        }
        
        body.append("\nWe'll notify you when your order ships!\n\n");
        body.append("Best regards,\nThe E-commerce Team");
        
        return body.toString();
    }
}
