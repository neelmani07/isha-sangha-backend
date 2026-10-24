package com.ishaworld.admin.dto;

/** One entry in the admin "Manage" list, whatever its kind. */
public record ManageRow(
        String kind,
        Long id,
        String name,
        String subtitle,
        /** Review status for member-submitted kinds; null where it doesn't apply. */
        String status,
        boolean active,
        Double lat,
        Double lng,
        /** Public page path, when the entry has one. */
        String publicPath
) {
}
