package com.mhp.coding.challenges.retry.inbound;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.inbound.NotificationHandler;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/v1/emails")
@AllArgsConstructor
public class EmailController {

    private final NotificationHandler notificationHandler;


    @PostMapping
    public ResponseEntity<EmailNotification> createEmailNotification(@RequestBody EmailNotification emailNotification) {
        EmailNotification emailNotificationResult = notificationHandler.processEmailNotification(emailNotification);
        return ResponseEntity.ok(emailNotificationResult);
    }
}
