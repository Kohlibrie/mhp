package com.mhp.coding.challenges.retry.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class QuartzConfiguration {

    public static final String CONTEXT_KEY = "applicationContext";

    private final ApplicationContext applicationContext;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setApplicationContextSchedulerContextKey(CONTEXT_KEY);
        scheduler.setConfigLocation(new ClassPathResource("quartz.properties"));
        scheduler.setAutoStartup(true);
        scheduler.setWaitForJobsToCompleteOnShutdown(true);

        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(this.applicationContext);
        scheduler.setJobFactory(jobFactory);

        return scheduler;
    }

}
