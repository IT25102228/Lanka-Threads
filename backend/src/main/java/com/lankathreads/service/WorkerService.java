package com.lankathreads.service;

import com.lankathreads.model.Worker;
import com.lankathreads.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service handling Factory Worker management:
 * Auto-generating Worker IDs (W001, W002...), phone formatting, and worker CRUD.
 */
@SuppressWarnings("null")
@Service
public class WorkerService {

    @Autowired
    private WorkerRepository workerRepository;

    /**
     * Retrieves all factory workers.
     */
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

    /**
     * Generates the next sequential Worker ID in format "W001", "W002", etc.
     * Uses the WorkerRegistrationManager Singleton pattern.
     */
    public String generateNextWorkerId() {
        return com.lankathreads.pattern.WorkerRegistrationManager.getInstance()
                .generateNextWorkerId(workerRepository.findAll());
    }

    /**
     * Validates Sri Lankan mobile numbers (e.g. 0771234567 or +94771234567).
     * Uses the WorkerRegistrationManager Singleton pattern.
     */
    public boolean isValidMobileNumber(String mobile) {
        return com.lankathreads.pattern.WorkerRegistrationManager.getInstance()
                .isValidMobileNumber(mobile);
    }

    /**
     * Formats mobile numbers into the readable standard: +94 7X 123 4567
     * Uses the WorkerRegistrationManager Singleton pattern.
     */
    public String formatMobileNumber(String mobile) {
        return com.lankathreads.pattern.WorkerRegistrationManager.getInstance()
                .formatMobileNumber(mobile);
    }

    /**
     * Saves a worker, assigning defaults for ID, status, and gender if not provided.
     * Utilizes the WorkerRegistrationManager Singleton to ensure thread-safe validation & preparation.
     */
    public Worker saveWorker(Worker worker) {
        List<Worker> existingWorkers = workerRepository.findAll();
        Worker preparedWorker = com.lankathreads.pattern.WorkerRegistrationManager.getInstance()
                .prepareWorkerForRegistration(worker, existingWorkers);
        return workerRepository.save(preparedWorker);
    }

    /**
     * Finds a worker by database primary key ID.
     */
    @Nullable
    public Worker getWorkerById(Long id) {
        return workerRepository.findById(id).orElse(null);
    }

    /**
     * Deletes a worker by database primary key ID.
     */
    public void deleteWorker(Long id) {
        workerRepository.deleteById(id);
    }
}
