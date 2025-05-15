package com.example.api.controller;

import com.example.api.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public abstract class BaseController {
    /**
     * Get the authenticated user ID from the security context
     *
     * @return The authenticated user's ID
     * @throws IllegalStateException if no authenticated user is found
     */
    protected UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null ||
                !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("No authenticated user found");
        }

        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    /**
     * Get the authenticated user from the security context
     *
     * @return The authenticated user
     * @throws IllegalStateException if no authenticated user is found
     */
    protected User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null ||
                !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("No authenticated user found");
        }

        return (User) authentication.getPrincipal();
    }
}
