package com.mhp.coding.challenges.retry.core.logic;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.inbound.NotificationHandler;
import com.mhp.coding.challenges.retry.core.outbound.NotificationSender;

@Service
public class NotificationService implements NotificationHandler {

    @Resource
    private NotificationSender notificationSender;

    @Override
    public EmailNotification processEmailNotification(EmailNotification emailNotification) {
        notificationSender.sendEmail(emailNotification);
        return emailNotification;
    }
}
