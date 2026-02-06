package com.thordata.sdk;

import java.time.Duration;

public class RunTaskConfig {
    public Duration maxWait = Duration.ofMinutes(10);
    public Duration initialPollInterval = Duration.ofSeconds(2);
    public Duration maxPollInterval = Duration.ofSeconds(10);

    public RunTaskConfig() {}

    public RunTaskConfig(Duration maxWait, Duration initialPollInterval, Duration maxPollInterval) {
        if (maxWait != null) this.maxWait = maxWait;
        if (initialPollInterval != null) this.initialPollInterval = initialPollInterval;
        if (maxPollInterval != null) this.maxPollInterval = maxPollInterval;
    }

    public RunTaskConfig maxWait(Duration d) { this.maxWait = d; return this; }
    public RunTaskConfig initialPollInterval(Duration d) { this.initialPollInterval = d; return this; }
    public RunTaskConfig maxPollInterval(Duration d) { this.maxPollInterval = d; return this; }
}