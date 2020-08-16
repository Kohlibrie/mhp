package com.mhp.coding.challenges.retry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.mhp.coding.challenges.retry.core.entities.EmailNotification;
import com.mhp.coding.challenges.retry.core.logic.RetryMailJob;

import lombok.SneakyThrows;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RetryApplicationTests {

    private static final String RETRY = "retry";

    private EmailNotification emailNotification;


    @Resource
    private RetryMailJob retryMailJob;

    @MockBean
    private Scheduler scheduler;

    @Before
    public void before() {
        emailNotification = EmailNotification.builder()
                .text("text")
                .subject("subject")
                .recipient("recipient")
                .build();
    }

    /**
     * Tests the different restart intervals. Starts at two, because too many calls (database, quartz scheduler) would have to be mocked for the method generateJobWithTrigger from the Service  JobGeneratorService.class.
     */
    @SneakyThrows
    @Test
    public void retryMechanism() {

        //prepare
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        Mockito.when(scheduler.scheduleJob(Mockito.any()))
                .thenReturn(DateTime.now()
                        .toDate());
        JobExecutionContext ctx = mock(JobExecutionContext.class);
        JobDetail jobDetail = generateJob(emailNotification);
        when(ctx.getJobDetail()).thenReturn(jobDetail);


        int retry = 2;
        JobDataMap map = new JobDataMap();
        map.put("retry", retry);
        List<Integer> retryIntervals = Arrays.asList(25, 125, 625);

        List<SimpleTrigger> simpleTriggerList = new ArrayList<>();
        DateTime now = DateTime.now();
        boolean firstRun = true;

        //action
        //Simulates four method calls to resend an email which all fail.
        for (int i = retry; i <= 5; i++) {
            int index = 0;
            SimpleTrigger simpleTrigger;
            if (firstRun) {
                simpleTrigger = generateTrigger(jobDetail, i, now.toDate());
            }
            else {
                simpleTrigger = generateTrigger(jobDetail, i, simpleTriggerList.get(index)
                        .getStartTime());
            }

            when(ctx.getTrigger()).thenReturn(simpleTrigger);
            simpleTriggerList.add(simpleTrigger);

            retryMailJob.execute(ctx);
            firstRun = false;
            index++;
        }


        //verify
        Mockito.verify(scheduler, times(5 - retry))
                .scheduleJob(triggerCaptor.capture());

        for (int i = retry; i <= 5; i++) {
            int index = 0;
            Assert.assertTrue(triggerCaptor.getAllValues()
                    .get(index)
                    .getStartTime()
                    .equals(new DateTime(simpleTriggerList.get(index)
                            .getStartTime()).plusSeconds(retryIntervals.get(index))
                            .toDate()));
            index++;
        }
    }

    //Helper functions
    private SimpleTrigger generateTrigger(JobDetail jobDetail, int retry, Date startTime) {
        return (SimpleTrigger) TriggerBuilder.newTrigger()
                .withIdentity("trigger-".concat(UUID.randomUUID()
                        .toString()), RETRY)
                .usingJobData(RETRY, retry)
                .startAt(startTime)
                .forJob(jobDetail)
                .build();
    }

    private JobDetail generateJob(EmailNotification emailNotification) {
        return JobBuilder.newJob(RetryMailJob.class)
                .withIdentity(new JobKey("job-".concat(UUID.randomUUID()
                        .toString()), RETRY))
                .storeDurably(true)
                .requestRecovery(true)
                .usingJobData(generateJobMapDateFromEmailNotifictaion(emailNotification))
                .build();
    }

    private JobDataMap generateJobMapDateFromEmailNotifictaion(EmailNotification emailNotification) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("recipient", emailNotification.getRecipient());
        jobDataMap.put("subject", emailNotification.getSubject());
        jobDataMap.put("text", emailNotification.getText());
        return jobDataMap;
    }

}
