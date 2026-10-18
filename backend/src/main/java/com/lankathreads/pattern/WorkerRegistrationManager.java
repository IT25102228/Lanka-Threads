package com.lankathreads.pattern;

import com.lankathreads.model.Worker;
import java.io.Serializable;
import java.util.List;

public class WorkerRegistrationManager implements Serializable {

    private static final long serialVersionUID = 1L;

    // Holds the single Singleton instance.
    private static volatile WorkerRegistrationManager instance;

    // Prevents direct or repeated constructor calls.
    private static boolean instantiated = false;

    private String lastGeneratedWorkerId = "W000";
    private int sessionRegisteredCount = 0;

    // Private constructor prevents object creation from outside the class.
    private WorkerRegistrationManager() {
        synchronized (WorkerRegistrationManager.class) {
            if (instantiated || instance != null) {
                throw new IllegalStateException(
                        "WorkerRegistrationManager is a Singleton! Use getInstance() instead.");
            }
            instantiated = true;
        }
    }

    // Provides global access to the single Singleton instance.
    public static WorkerRegistrationManager getInstance() {
        if (instance == null) {
            synchronized (WorkerRegistrationManager.class) {
                if (instance == null) {
                    instance = new WorkerRegistrationManager();
                }
            }
        }
        return instance;
    }

    // Preserves the Singleton instance after deserialization.
    protected Object readResolve() {
        return getInstance();
    }

    // Generates the next unique worker ID based on existing workers.
    public synchronized String generateNextWorkerId(List<Worker> existingWorkers) {
        long maxId = 0;

        if (existingWorkers != null) {
            for (Worker worker : existingWorkers) {
                String workerId = worker.getWorkerId();

                // Checks whether the worker ID follows the W-number format.
                if (workerId != null && workerId.toUpperCase().startsWith("W")) {
                    try {
                        long number = Long.parseLong(workerId.substring(1).trim());

                        if (number > maxId) {
                            maxId = number;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        String nextId = String.format("W%03d", maxId + 1);
        this.lastGeneratedWorkerId = nextId;

        return nextId;
    }

    // Validates a Sri Lankan mobile number.
    public boolean isValidMobileNumber(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }

        String cleaned = mobile.replaceAll("[\\s()\\-\\.]", "");

        return cleaned.matches("^(\\+94|94|0)?7[0-9]{8}$");
    }

    // Converts different mobile number formats into a standard format.
    public String formatMobileNumber(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return "";
        }

        String cleaned = mobile.replaceAll("[\\s()\\-\\.]", "");

        if (cleaned.startsWith("+94") && cleaned.length() == 12) {
            return "+94 " + cleaned.substring(3, 5) + " "
                    + cleaned.substring(5, 8) + " "
                    + cleaned.substring(8);
        } else if (cleaned.startsWith("94") && cleaned.length() == 11) {
            return "+94 " + cleaned.substring(2, 4) + " "
                    + cleaned.substring(4, 7) + " "
                    + cleaned.substring(7);
        } else if (cleaned.startsWith("0") && cleaned.length() == 10) {
            return "+94 " + cleaned.substring(1, 3) + " "
                    + cleaned.substring(3, 6) + " "
                    + cleaned.substring(6);
        } else if (cleaned.length() == 9 && cleaned.startsWith("7")) {
            return "+94 " + cleaned.substring(0, 2) + " "
                    + cleaned.substring(2, 5) + " "
                    + cleaned.substring(5);
        }

        return mobile.trim();
    }

    // Prepares worker data before registration.
    public synchronized Worker prepareWorkerForRegistration(
            Worker worker,
            List<Worker> existingWorkers) {

        // Automatically generates an ID when one is not provided.
        if (worker.getWorkerId() == null || worker.getWorkerId().trim().isEmpty()) {
            worker.setWorkerId(generateNextWorkerId(existingWorkers));
        }

        // Formats the mobile number before saving the worker.
        if (worker.getMobileNumber() != null
                && !worker.getMobileNumber().trim().isEmpty()) {
            worker.setMobileNumber(formatMobileNumber(worker.getMobileNumber()));
        }

        // Sets the default gender when no value is provided.
        if (worker.getGender() == null || worker.getGender().trim().isEmpty()) {
            worker.setGender("Male");
        }

        // Sets the default worker status.
        if (worker.getStatus() == null || worker.getStatus().trim().isEmpty()) {
            worker.setStatus("Active");
        }

        sessionRegisteredCount++;

        return worker;
    }

    // Returns the most recently generated worker ID.
    public String getLastGeneratedWorkerId() {
        return lastGeneratedWorkerId;
    }

    // Returns the number of workers registered in the current session.
    public int getSessionRegisteredCount() {
        return sessionRegisteredCount;
    }
}