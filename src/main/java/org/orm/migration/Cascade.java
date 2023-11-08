package org.orm.migration;

/**
 * @author Luís Felipe Rocha Martins
 * @since  November 2023
 */
public enum Cascade {
    PERSIST,
    MERGE,
    REMOVE,
    REFRESH,
    ALL;
}
