/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.quartz.impl;

import gtanks.system.quartz.QuartzJob;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import gtanks.system.quartz.impl.QuartzJobRunner;
import java.util.Date;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzServiceImpl
implements QuartzService {
    private static QuartzServiceImpl instance = new QuartzServiceImpl();
    private Scheduler scheduler;

    private QuartzServiceImpl() {
        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
            this.scheduler = schedulerFactory.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException schedulerException) {
            // empty catch block
        }
    }

    private JobDetail createJob(String name, String group2, QuartzJob object) {
        JobDetail job = new JobDetail(name, group2, QuartzJobRunner.class);
        job.getJobDataMap().put((Object)QuartzJobRunner.jobRunKey, object);
        return job;
    }

    @Override
    public JobDetail addJobInterval(String name, String group2, QuartzJob object, TimeType type, long interval, int repeatCount) {
        JobDetail job = this.createJob(name, group2, object);
        try {
            SimpleTrigger trigger = new SimpleTrigger(name, group2, repeatCount, type.time(interval));
            this.scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException schedulerException) {
            // empty catch block
        }
        return job;
    }

    @Override
    public JobDetail addJobInterval(String name, String group2, QuartzJob object, TimeType type, long interval) {
        return this.addJobInterval(name, group2, object, type, interval, -1);
    }

    @Override
    public JobDetail addJob(String name, String group2, QuartzJob object, TimeType type, long time) {
        JobDetail job = this.createJob(name, group2, object);
        try {
            SimpleTrigger trigger = new SimpleTrigger(name, group2, new Date(System.currentTimeMillis() + type.time(time)));
            this.scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException schedulerException) {
            // empty catch block
        }
        return job;
    }

    @Override
    public void deleteJob(String name, String group2) {
        try {
            this.scheduler.deleteJob(name, group2);
        } catch (SchedulerException schedulerException) {
            // empty catch block
        }
    }

    public static QuartzService inject() {
        return instance;
    }
}

