package com.myfirstproject.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AsyncComputationService {

    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "async-computation");
        thread.setDaemon(true);
        return thread;
    });

    public CompletableFuture<ComputationResult> performLongCalculation(String taskName, int durationSeconds) {
        return CompletableFuture.supplyAsync(() -> runCalculation(taskName, durationSeconds,
                "Calculation finished after %d seconds"), executor);
    }

    public CompletableFuture<ComputationResult> performCalculationWithDbAccess(String taskName, int durationSeconds) {
        return CompletableFuture.supplyAsync(() -> runCalculation(taskName, durationSeconds,
                "Async calculation with DB access completed after %d seconds. " +
                        "EntityManager scope: demo uses application-managed worker thread."), executor);
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    private ComputationResult runCalculation(String taskName, int durationSeconds, String resultTemplate) {
        try {
            long startTime = System.currentTimeMillis();

            // Simulate long-running calculation
            Thread.sleep(durationSeconds * 1000L);

            long endTime = System.currentTimeMillis();
            long actualDuration = (endTime - startTime) / 1000;

            ComputationResult result = new ComputationResult();
            result.setTaskName(taskName);
            result.setStatus("COMPLETED");
            result.setResult(String.format(resultTemplate, actualDuration));
            result.setDurationSeconds(actualDuration);
            result.setTimestamp(System.currentTimeMillis());
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            ComputationResult result = new ComputationResult();
            result.setTaskName(taskName);
            result.setStatus("INTERRUPTED");
            result.setResult("Task was interrupted: " + e.getMessage());
            result.setDurationSeconds(0);
            result.setTimestamp(System.currentTimeMillis());
            return result;
        }
    }

    public static class ComputationResult {
        private String taskName;
        private String status;
        private String result;
        private long durationSeconds;
        private long timestamp;

        // Getters and setters
        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public long getDurationSeconds() {
            return durationSeconds;
        }

        public void setDurationSeconds(long durationSeconds) {
            this.durationSeconds = durationSeconds;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
