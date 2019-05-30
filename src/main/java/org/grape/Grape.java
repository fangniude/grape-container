package org.grape;

import org.springframework.context.ApplicationContext;

/**
 * Grape interface
 *
 * @author lewis
 */
public interface Grape {
    /**
     * name
     *
     * @return use package name default
     */
    default String name() {
        return getClass().getPackage().getName();
    }

    /**
     * need db migration or not
     *
     * @return default true
     */
    default boolean hasEntity() {
        return true;
    }

    /**
     * before grape application run
     */
    default void inTheBeginning() {
    }

    /**
     * after database migration
     */
    default void afterDataBaseMigration() {
    }

    /**
     * after spring loaded
     *
     * @param context spring application context
     */
    default void afterStarted(ApplicationContext context) {
    }
}
