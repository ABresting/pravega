/**
 * Copyright (c) 2017 Dell Inc., or its subsidiaries.
 */

package com.emc.pravega.framework.tasks;

import com.emc.pravega.framework.TestFrameworkException;
import com.emc.pravega.framework.metronome.model.v1.Job;
import com.emc.pravega.framework.metronome.model.v1.Restart;
import com.emc.pravega.framework.metronome.model.v1.Run;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.emc.pravega.framework.TestFrameworkException.Type.InternalError;

@Slf4j
public class CommandTask extends MetronomeBasedTask {

    private final String command;
    private final double cpu;
    private final double mem;
    private final int disk;

    public CommandTask(final String id, final String command) {
      this(id, command, 0.5, 64.0, 0);
    }

    public CommandTask(final String id, final String command, final double cpu, final double mem, final int disk) {
        super(id);
        this.command = command;
        this.cpu = cpu;
        this.mem = mem;
        this.disk = disk;
    }

    @Override
    public void start() {
        log.info("Starting execution of Command Task with id : {}", id);
        Job cmd = createJob(id, cpu, mem, disk);
        try {
            startTaskExecution(cmd).get(5, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            log.error("Error while execution Command Task with id : {}", id, ex);
            throw new TestFrameworkException(InternalError, "Error executing command Task.", ex);
        }
    }

    @Override
    public void stop() {
        throw new NotImplementedException("Stopping a Metronome task is not implemented");
    }

    private Job createJob(final String id, final double cpu, final double mem, final int disk) {
        Map<String, String> labels = new HashMap<>(1);
        labels.put("name", "Command task for SystemTest framework");

        //This can be used to set environment variables while executing the job on Metronome.
        Map<String, String> env = new HashMap<>(1);
        env.put("masterIP", System.getProperty("masterIP"));

        Restart restart = new Restart();
        restart.setActiveDeadlineSeconds(120); // the task is expected to  finish in 2 mins
        restart.setPolicy("NEVER");

        Run run = new Run();
        run.setArtifacts(Collections.emptyList());
        run.setCmd("/bin/bash -c " + command.replaceAll(" ", "\\ ")); // enable multiple commands seperated by ;

        run.setCpus(cpu);
        run.setMem(mem);
        run.setDisk(disk);
        run.setEnv(env);
        run.setMaxLaunchDelay(3600);
        run.setRestart(restart);
        run.setUser("root");

        Job job = new Job();
        job.setId(id);
        job.setDescription(id);
        job.setLabels(labels);
        job.setRun(run);

        return job;
    }
}
