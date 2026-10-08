package com.sims.util;

/**
 * Custom checked exception for input validation failures.
 *
 * <p>Thrown by {@link com.sims.controller.StudentController} when user-submitted
 * fields fail business rules, regular expression patterns, or range checks.</p>
 */
public class ValidationException extends Exception {

    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
