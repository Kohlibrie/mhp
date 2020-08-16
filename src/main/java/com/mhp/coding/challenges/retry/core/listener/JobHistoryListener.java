package com.mhp.coding.challenges.retry.core.listener;


import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.mhp.coding.challenges.retry.configuration.QuartzConfiguration;
import com.mhp.coding.challenges.retry.core.repository.JobHistoryRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Listens on quartz Job lifecycle events to save them into a
 * MongoDB history collection, only finished jobs (whether successful or not are saved).
 *
 * @author Niko Schmuck
 * @see org.quartz.plugins.history.LoggingJobHistoryPlugin
 */
@Slf4j
public class JobHistoryListener implements SchedulerPlugin, JobListener {

    private String name;
    private Scheduler scheduler;
    private JobHistoryRepository repository;

    public void initialize(String pname, Scheduler scheduler, ClassLoadHelper classLoadHelper) throws SchedulerException {
        this.name = pname;
        this.scheduler = scheduler;
        scheduler.getListenerManager()
                .addJobListener(this, EverythingMatcher.allJobs());
    }

    public String getName() {
        return name;
    }

    public void start() {
        // retrieve Spring application context to setup
        try {
            log.debug("Available context keys: {}", Arrays.asList(scheduler.getContext()
                    .getKeys()));
            ApplicationContext ctx = (ApplicationContext) scheduler.getContext()
                    .get(QuartzConfiguration.CONTEXT_KEY);
            log.info("Retrieving mongo client from context: {}", Arrays.asList(scheduler.getContext()
                    .getKeys()));
            repository = ctx.getBean(JobHistoryRepository.class);
        }
        catch (SchedulerException e) {
            log.error("Unable to retrieve application context from quartz scheduler", e);
        }
    }

    public void shutdown() {
        // nothing to do...
    }

    public void jobToBeExecuted(JobExecutionContext context) {
        // nothing to do...
    }

    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        log.info("jobWasExecuted :: {}", context);
        HashMap<String, Object> map = new HashMap<>();
        map.putAll(getDefaultKeys(context));
        if (StringUtils.isEmpty(jobException))
            map.put("result", String.valueOf(context.getResult()));
        else {
            map.put("errMsg", jobException.getMessage());
            map.put("jobException", jobException.getMessage());
        }
        repository.add(map);

    }

    public void jobExecutionVetoed(JobExecutionContext context) {
        log.info("jobExecutionVetoed :: {}", context);
        HashMap<String, Object> map = new HashMap<>();
        map.putAll(getDefaultKeys(context));
        map.put("veto", true);

        repository.add(map);
    }

    private HashMap<String, Object> getDefaultKeys(JobExecutionContext context) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("ts", new Date());
        map.put("name", context.getJobDetail()
                .getKey()
                .getName());
        map.put("group", context.getJobDetail()
                .getKey()
                .getGroup());
        map.put("started", context.getFireTime());
        map.put("runtime", context.getJobRunTime());
        map.put("refireCount", context.getRefireCount());

        return map;
    }

}
