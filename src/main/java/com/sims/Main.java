package com.sims;

import com.sims.view.StudentManagementFrame;

import javax.swing.*;

/**
 * Application entry point for the Student Information Management System (SIMS).
 *
 * <h2>Lab Information</h2>
 * <ul>
 *   <li>Course: UIT3361 – Object-Oriented Programming using Java</li>
 *   <li>Component: Standalone GUI Desktop Application with Oracle JDBC</li>
 * </ul>
 *
 * <h2>Swing Threading Rules</h2>
 * <p>All Swing GUI component instantiation and modification is scheduled on
 * the Event Dispatch Thread (EDT) using {@link SwingUtilities#invokeLater(Runnable)}.</p>
 */
public class Main {

    public static void main(String[] args) {
        // Set native system Look and Feel for clean, professional rendering
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default Swing Metal L&F if system L&F cannot be set
        }

        // Launch main window safely on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            StudentManagementFrame frame = new StudentManagementFrame();
            frame.setVisible(true);
        });
    }
}
