package com.lankathreads.pattern;

import com.lankathreads.model.Worker;
import com.lankathreads.pattern.WorkerRegistrationManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WorkerRegistrationManager Singleton Pattern Tests")
public class WorkerRegistrationManagerTest {

    @Test
    @DisplayName("Singleton Test: getInstance() should always return the same instance")
    public void testSingletonInstanceEquality() {
        WorkerRegistrationManager instance1 = WorkerRegistrationManager.getInstance();
        WorkerRegistrationManager instance2 = WorkerRegistrationManager.getInstance();

        assertNotNull(instance1, "Instance 1 should not be null");
        assertNotNull(instance2, "Instance 2 should not be null");
        assertSame(instance1, instance2, "Both instances must reference the exact same memory address (Singleton)");
    }

    @Test
    @DisplayName("Singleton Defense: Reflection instantiation should throw exception")
    public void testReflectionProtection() {
        WorkerRegistrationManager.getInstance(); // Ensure the singleton is initialized
        assertThrows(InvocationTargetException.class, () -> {
            Constructor<WorkerRegistrationManager> constructor = 
                    WorkerRegistrationManager.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }, "Instantiating a second Singleton instance via reflection must be prohibited");
    }

    @Test
    @DisplayName("Worker ID Sequence: Generates W001 when workforce list is empty")
    public void testSequentialIdGenerationEmpty() {
        WorkerRegistrationManager manager = WorkerRegistrationManager.getInstance();
        String nextId = manager.generateNextWorkerId(new ArrayList<>());
        assertEquals("W001", nextId, "First worker ID should be W001");
    }

    @Test
    @DisplayName("Worker ID Sequence: Correctly computes next sequential ID based on existing workers")
    public void testSequentialIdGenerationWithExisting() {
        WorkerRegistrationManager manager = WorkerRegistrationManager.getInstance();
        List<Worker> workers = new ArrayList<>();

        Worker w1 = new Worker();
        w1.setWorkerId("W001");
        workers.add(w1);

        Worker w2 = new Worker();
        w2.setWorkerId("W005");
        workers.add(w2);

        String nextId = manager.generateNextWorkerId(workers);
        assertEquals("W006", nextId, "Next worker ID should be W006 based on max existing ID");
    }

    @Test
    @DisplayName("Mobile Validation: Validates Sri Lankan mobile number formats")
    public void testMobileValidation() {
        WorkerRegistrationManager manager = WorkerRegistrationManager.getInstance();

        assertTrue(manager.isValidMobileNumber("0771234567"), "0771234567 should be valid");
        assertTrue(manager.isValidMobileNumber("+94 77 123 4567"), "+94 77 123 4567 should be valid");
        assertTrue(manager.isValidMobileNumber("94712345678"), "94712345678 should be valid");
        assertFalse(manager.isValidMobileNumber("12345"), "Invalid short number should fail");
        assertFalse(manager.isValidMobileNumber("0112345678"), "Landline number should not be a valid mobile");
    }

    @Test
    @DisplayName("Worker Preparation: Enforces defaults and formats contact numbers")
    public void testWorkerPreparation() {
        WorkerRegistrationManager manager = WorkerRegistrationManager.getInstance();
        Worker worker = new Worker();
        worker.setName("Test Worker");
        worker.setMobileNumber("0771234567");

        Worker prepared = manager.prepareWorkerForRegistration(worker, new ArrayList<>());

        assertEquals("W001", prepared.getWorkerId(), "Should assign generated W001 ID");
        assertEquals("+94 77 123 4567", prepared.getMobileNumber(), "Should format phone number");
        assertEquals("Male", prepared.getGender(), "Should assign default gender Male");
        assertEquals("Active", prepared.getStatus(), "Should assign default status Active");
    }
}
