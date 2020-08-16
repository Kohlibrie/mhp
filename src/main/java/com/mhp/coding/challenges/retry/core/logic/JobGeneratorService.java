package com.mhp.coding.challenges.retry.core.logic;

import static org.quartz.DateBuilder.futureDate;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@NoArgsConstructor
public class JobGeneratorService {

    private static final String RETRY = "retry";

    @Resource
    private Scheduler scheduler;

    /**
     * Create job and trigger for resending mail
     *
     * @param emailNotification
     */
    @SneakyThrows
    public void generateJobWithTrigger(EmailNotification emailNotification) {
        //Create and add job to scheduler
        JobDetail jobDetail = generateJob(emailNotification);
        scheduler.addJob(jobDetail, true, true);

        //Schedule Job
        scheduler.scheduleJob(generateTrigger(jobDetail));
    }

    /**
     * Create new trigger based on the number of repetitions
     *
     * @param previousTrigger
     * @param jobDetail
     * @param retry
     */
    @SneakyThrows
    public void reCreateTrigger(Trigger previousTrigger, JobDetail jobDetail, int retry) {
        Date triggerDate = new DateTime(previousTrigger.getStartTime()).plusSeconds(getNextInterval(retry))
                .toDate();
        scheduler.scheduleJob(generateTrigger(jobDetail, retry, triggerDate));
    }


    /**
     * Create the first trigger
     * @param jobDetail
     * @return
     */
    private Trigger generateTrigger(JobDetail jobDetail) {
        final int retry = 1;
        return generateTrigger(jobDetail, retry, futureDate(getNextInterval(retry), DateBuilder.IntervalUnit.SECOND));
    }

    /**
     * Create trigger and calculate next fire time based on the retries
     *
     * @param jobDetail
     * @param retry
     * @return
     */
    private SimpleTrigger generateTrigger(JobDetail jobDetail, Integer retry, Date triggerDate) {
        return (SimpleTrigger) TriggerBuilder.newTrigger()
                .withIdentity("trigger-".concat(UUID.randomUUID()
                        .toString()), RETRY)
                .usingJobData(RETRY, retry)
                .startAt(triggerDate)
                .forJob(jobDetail)
                .build();
    }

    /**
     * Calculates the next trigger interval
     * @param retry
     * @return
     */
    private Integer getNextInterval(Integer retry) {
        return (int) Math.pow(5, retry);
    }

    /**
     * Create a job with all relevant informations
     * @param emailNotification
     * @return
     */
    private JobDetail generateJob(EmailNotification emailNotification) {
        return JobBuilder.newJob(RetryMailJob.class)
                .withIdentity(new JobKey("job-".concat(UUID.randomUUID()
                        .toString()), RETRY))
                .storeDurably(true)
                .requestRecovery(true)
                .usingJobData(generateJobMapDataFromEmailNotifictaion(emailNotification))
                .build();
    }

    /**
     * Create a jobData map which holds the email data
     *
     * @param emailNotification
     * @return
     */
    private JobDataMap generateJobMapDataFromEmailNotifictaion(EmailNotification emailNotification) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("recipient", emailNotification.getRecipient());
        jobDataMap.put("subject", emailNotification.getSubject());
        jobDataMap.put("text", emailNotification.getText());
        return jobDataMap;
    }
}
