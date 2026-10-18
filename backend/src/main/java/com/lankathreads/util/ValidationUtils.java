package com.lankathreads.util;

import java.util.regex.Pattern;

/**
 * Utility class for common input validations (Email, Phone, Contact Info).
 * Designed to be simple, readable, and easy to explain.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Utility class - prevent instantiation
    }

    // Standard email pattern: username@domain.extension (e.g., user@example.com)
    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Checks if the given email string has a valid format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return SIMPLE_EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Removes spaces, dashes, dots, and parentheses from a phone string.
     */
    public static String normalizePhoneNumber(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[\\s()\\-\\.]", "");
    }

    /**
     * Validates Sri Lankan and general international phone numbers.
     * Examples of valid formats:
     * - Sri Lankan mobile: 0771234567, +94771234567, 94771234567
     * - Sri Lankan landline: 0112345678, +94112345678
     * - International: 8 to 15 digits
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        String cleaned = normalizePhoneNumber(phone);

        // Check 1: Sri Lankan mobile number (starts with 07, 7, +947, or 947 followed by 8 digits)
        if (cleaned.matches("^(\\+94|94|0)?7[0-9]{8}$")) {
            return true;
        }

        // Check 2: Sri Lankan landline number (starts with 0 or +94 followed by 9 digits)
        if (cleaned.matches("^(\\+94|94|0)[1-9][0-9]{7,8}$")) {
            return true;
        }

        // Check 3: Standard international phone format (8 to 15 digits, optional '+' at start)
        if (cleaned.matches("^\\+?[0-9]{8,15}$")) {
            return true;
        }

        return false;
    }

    /**
     * Validates supplier contact info which can contain an email, phone, or both separated by commas/slashes.
     */
    public static boolean isValidContactInfo(String contactInfo) {
        if (contactInfo == null || contactInfo.trim().isEmpty()) {
            return false;
        }

        // Split by common separators: comma, semicolon, slash
        String[] parts = contactInfo.split("[,;/]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (isValidEmail(trimmed) || isValidPhoneNumber(trimmed)) {
                return true;
            }
        }

        return isValidEmail(contactInfo.trim()) || isValidPhoneNumber(contactInfo.trim());
    }
}
