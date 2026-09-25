package com.complaintsystem.app;

import com.complaintsystem.tracker.ComplaintTracker;

import java.time.LocalDate;

/**
 * Application Entry Point for the Complaint Management System.
 * Initializes core models, seeds sample records, and starts the Console UI.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Complaint Management System...");

        // Initialize Tracker and virtual clock
        ComplaintTracker tracker = new ComplaintTracker();
        LocalDate initialSimulatedDate = LocalDate.now();

        // Seed with sample users, admins, and complaints
        tracker.seedSampleData(initialSimulatedDate);

        // Launch Console User Interface
        ConsoleUI ui = new ConsoleUI(tracker, initialSimulatedDate);
        ui.start();
    }
}
