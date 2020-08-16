package com.mhp.coding.challenges.retry.core.logic;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@NoArgsConstructor
public class RetryMailJob implements Job {

    private static final Integer MAX_RETRY = 5;

    @Value("${retry.mechanism.senderadress:default}")
    private String senderAdress;

    private MailSender mailSender;

    private JobGeneratorService jobGeneratorService;

    @Autowired
    public RetryMailJob(MailSender mailSender, JobGeneratorService jobGeneratorService) {
        this.mailSender = mailSender;
        this.jobGeneratorService = jobGeneratorService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail()
                .getJobDataMap();

        int retry = context.getTrigger()
                .getJobDataMap()
                .getIntValue("retry");

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(senderAdress.concat(String.format("- %d", retry)));
            mailMessage.setTo(jobDataMap.getString("recipient"));
            mailMessage.setSubject(jobDataMap.getString("subject"));
            mailMessage.setText(jobDataMap.getString("text"));

            mailSender.send(mailMessage);
        }
        catch (Exception e) {

            if (retry < MAX_RETRY) jobGeneratorService.reCreateTrigger(context.getTrigger(), context.getJobDetail(), retry);
            else {
                log.error(String.format("Could not send Mail after %d retries", MAX_RETRY));
            }
        }
    }
}
