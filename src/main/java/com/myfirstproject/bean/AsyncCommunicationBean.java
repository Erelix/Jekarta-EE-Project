package com.myfirstproject.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.myfirstproject.service.AsyncComputationService;
import com.myfirstproject.service.AsyncComputationService.ComputationResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("asyncDemo")
@ApplicationScoped
public class AsyncCommunicationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private AsyncComputationService asyncService;

    private final List<AsyncTaskTracker> activeTasks = Collections.synchronizedList(new ArrayList<>());
    private final transient Map<String, CompletableFuture<ComputationResult>> futureMap = Collections.synchronizedMap(new HashMap<>());
    private int durationSeconds = 5;
    private String taskName = "Task";
    private String selectedTaskId;
    private ComputationResult selectedResult;
    private String pollMessage = "";

    public String startAsyncTask() {
        try {
            selectedResult = null;
            String taskId = "TASK_" + System.currentTimeMillis();
            CompletableFuture<ComputationResult> future = asyncService.performLongCalculation(
                taskName + "_" + taskId,
                durationSeconds
            );
            futureMap.put(taskId, future);
            AsyncTaskTracker tracker = new AsyncTaskTracker(taskId, taskName, durationSeconds);
            activeTasks.add(tracker);
            pollMessage = "Task '" + taskName + "' started with ID: " + taskId;

        } catch (Exception e) {
            pollMessage = "Error starting task: " + e.getMessage();
        }
        return null;
    }

    public String startAsyncTaskWithDb() {
        try {
            selectedResult = null;
            String taskId = "DB_TASK_" + System.currentTimeMillis();
            CompletableFuture<ComputationResult> future = asyncService.performCalculationWithDbAccess(
                taskName + "_" + taskId,
                durationSeconds
            );
            futureMap.put(taskId, future);
            AsyncTaskTracker tracker = new AsyncTaskTracker(taskId, taskName, durationSeconds);
            activeTasks.add(tracker);
            pollMessage = "DB Access Task '" + taskName + "' started with ID: " + taskId;

        } catch (Exception e) {
            pollMessage = "Error starting task: " + e.getMessage();
        }
        return null;
    }

    public String pollTask() {
        if (selectedTaskId == null || selectedTaskId.isEmpty()) {
            pollMessage = "Please select a task to poll";
            return null;
        }

        CompletableFuture<ComputationResult> future = futureMap.get(selectedTaskId);
        if (future == null) {
            pollMessage = "Task not found: " + selectedTaskId;
            return null;
        }

        AsyncTaskTracker tracker = findTask(selectedTaskId);
        if (tracker == null) {
            pollMessage = "Task tracker not found: " + selectedTaskId;
            return null;
        }

        try {
            if (future.isDone()) {
                try {
                    selectedResult = future.get();
                    tracker.setStatus("DONE");
                    pollMessage = "Task completed! Result: " + selectedResult.getResult();
                } catch (Exception e) {
                    tracker.setStatus("ERROR");
                    pollMessage = "Task ended with error: " + e.getMessage();
                }
            } else {
                selectedResult = null;
                pollMessage = "Task still running... (" + tracker.getElapsedSeconds() + "s elapsed)";
                tracker.setStatus("RUNNING");
            }
        } catch (Exception e) {
            pollMessage = "Error polling task: " + e.getMessage();
        }

        return null;
    }

    public String cancelTask() {
        if (selectedTaskId == null || selectedTaskId.isEmpty()) {
            pollMessage = "Please select a task to cancel";
            return null;
        }

        CompletableFuture<ComputationResult> future = futureMap.get(selectedTaskId);
        if (future == null) {
            pollMessage = "Task not found: " + selectedTaskId;
            return null;
        }

        AsyncTaskTracker tracker = findTask(selectedTaskId);
        if (tracker == null) {
            pollMessage = "Task tracker not found: " + selectedTaskId;
            return null;
        }

        boolean cancelled = future.cancel(true);
        if (cancelled) {
            tracker.setStatus("CANCELLED");
            pollMessage = "Task cancelled successfully";
        } else {
            pollMessage = "Could not cancel task (already completed or running)";
        }

        return null;
    }

    public String refreshAllTasks() {
        StringBuilder status = new StringBuilder();
        int runningCount = 0;
        int doneCount = 0;

        for (AsyncTaskTracker tracker : activeTasks) {
            CompletableFuture<ComputationResult> future = futureMap.get(tracker.getId());
            if (future != null && future.isDone()) {
                tracker.setStatus("DONE");
                doneCount++;
            } else if (future != null) {
                tracker.setStatus("RUNNING");
                runningCount++;
            }
        }

        status.append(String.format("Running: %d tasks | Completed: %d tasks", runningCount, doneCount));
        pollMessage = status.toString();
        return null;
    }

    public String clearCompleted() {
        activeTasks.removeIf(tracker -> {
            CompletableFuture<ComputationResult> future = futureMap.get(tracker.getId());
            return future != null && future.isDone();
        });
        pollMessage = "Cleared completed tasks";
        return null;
    }

    private AsyncTaskTracker findTask(String taskId) {
        return activeTasks.stream()
            .filter(t -> t.getId().equals(taskId))
            .findFirst()
            .orElse(null);
    }

    // Getters and setters
    public List<AsyncTaskTracker> getActiveTasks() {
        return activeTasks;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getSelectedTaskId() {
        return selectedTaskId;
    }

    public void setSelectedTaskId(String selectedTaskId) {
        this.selectedTaskId = selectedTaskId;
    }

    public ComputationResult getSelectedResult() {
        return selectedResult;
    }

    public void setSelectedResult(ComputationResult selectedResult) {
        this.selectedResult = selectedResult;
    }

    public String getPollMessage() {
        return pollMessage;
    }

    public void setPollMessage(String pollMessage) {
        this.pollMessage = pollMessage;
    }

    // Helper methods for EL expressions
    public boolean isPollMessageStarted() {
        return pollMessage != null && pollMessage.contains("started");
    }

    public boolean isPollMessageError() {
        return pollMessage != null && (pollMessage.contains("Error") || pollMessage.contains("not found"));
    }

    public boolean isPollMessageRunning() {
        return pollMessage != null && pollMessage.contains("Running");
    }

    public boolean isPollMessageCompleted() {
        return pollMessage != null && (pollMessage.contains("completed") || pollMessage.contains("SUCCESS"));
    }

    public boolean isPollMessageWarning() {
        return pollMessage != null && (pollMessage.contains("Tasks") || pollMessage.contains("cancelled"));
    }

    // Inner class for tracking async tasks
    public static class AsyncTaskTracker implements Serializable {
        private static final long serialVersionUID = 1L;

        private String id;
        private String name;
        private long startTime;
        private String status;
        private long durationSeconds;

        public AsyncTaskTracker(String id, String name, long durationSeconds) {
            this.id = id;
            this.name = name;
            this.startTime = System.currentTimeMillis();
            this.status = "QUEUED";
            this.durationSeconds = durationSeconds;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public long getElapsedSeconds() {
            return (System.currentTimeMillis() - startTime) / 1000;
        }

        public long getDurationSeconds() {
            return durationSeconds;
        }

        public long getRemainingSeconds() {
            long remaining = durationSeconds - getElapsedSeconds();
            return remaining > 0 ? remaining : 0;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
