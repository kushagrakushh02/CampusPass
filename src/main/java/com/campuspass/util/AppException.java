package com.campuspass.util;

/** A business-rule problem whose message is safe to show to the user (e.g. "no seats left"). */
public class AppException extends Exception {
    private static final long serialVersionUID = 1L;
    public AppException(String message) { super(message); }
}
