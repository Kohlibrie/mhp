package com.mhp.coding.challenges.retry.outbound;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.logic.JobGeneratorService;
import com.mhp.coding.challenges.retry.core.outbound.NotificationSender;

@Service
@Validated
public class EmailNotificationSenderService implements NotificationSender {

    @Value("${retry.mechanism.senderadress:default}")
    private String senderAdress;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private JobGeneratorService jobGeneratorService;


    @Async
    @Override
    public void sendEmail(@Valid @NotNull EmailNotification emailNotification) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(senderAdress);
            mailMessage.setTo(emailNotification.getRecipient());
            mailMessage.setSubject(emailNotification.getSubject());
            mailMessage.setText(emailNotification.getText());

            mailSender.send(mailMessage);
        }
        catch (Exception e) {
            jobGeneratorService.generateJobWithTrigger(emailNotification);
            throw new RuntimeException(String.format("Failed to send email to recipient: %s", emailNotification.getRecipient()));
        }
    }
}
