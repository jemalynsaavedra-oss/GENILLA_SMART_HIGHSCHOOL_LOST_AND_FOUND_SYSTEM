package com.smartlostfound.model;

/**
 * Every role that can exist in the system.
 * Matches Section 2 of the approved System Proposal: Admin, Finder, Owner.
 * The database CHECK constraint and the registration menu are both generated from this enum.
 */
public enum Role {
    ADMIN,
    FINDER,
    OWNER
}
