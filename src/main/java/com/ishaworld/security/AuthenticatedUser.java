package com.ishaworld.security;

/**
 * Principal for a signed-in public (non-admin) user.
 */
public record AuthenticatedUser(
        Long id,
        String email
) {
}
