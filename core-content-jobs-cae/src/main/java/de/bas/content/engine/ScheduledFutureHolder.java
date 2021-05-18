package de.bas.content.engine;

import de.bas.content.jobs.AbstractContentJob;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Markus Schwarz
 */
public class ScheduledFutureHolder {
    ScheduledFuture<?> future;
    AbstractContentJob abstractContentJob;

    public ScheduledFutureHolder(ScheduledFuture<?> future, AbstractContentJob abstractContentJob) {
        this.future = future;
        this.abstractContentJob = abstractContentJob;
    }

    public AbstractContentJob getAbstractContentJob() {
        return abstractContentJob;
    }
}
