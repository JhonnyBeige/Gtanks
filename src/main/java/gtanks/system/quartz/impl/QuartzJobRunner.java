/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.quartz.impl;

import gtanks.system.quartz.QuartzJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class QuartzJobRunner
implements Job {
    public static String jobRunKey = "runnable";

    @Override
    public void execute(JobExecutionContext context) {
        QuartzJob run = (QuartzJob)context.getJobDetail().getJobDataMap().get(jobRunKey);
        run.run(context);
    }
}

