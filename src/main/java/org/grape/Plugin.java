package org.grape;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

/**
 * Plugin interface
 *
 * @author lewis
 */
public abstract class Plugin {
    private final String name = getClass().getPackage().getName();

    @Getter
    @Setter
    private String dbName = GrapeApplication.DATA_SOURCE_DEFAULT;

    /**
     * name
     *
     * @return use package name
     */
    public String name() {
        return name;
    }

    public EbeanServer ebeanServer() {
        if (hasEntity()) {
            return Ebean.getServer(dbName);
        } else {
            throw new GrapeException(String.format("plugin: %s not has entity, unsupported this method", name));
        }
    }

    /**
     * need db migration or not
     *
     * @return default true
     */
    protected boolean hasEntity() {
        return true;
    }

    /**
     * before grape application run
     */
    protected void inTheBeginning() {
    }

    /**
     * after database migration
     */
    protected void afterDataBaseMigration() {
    }

    /**
     * after spring loaded
     *
     * @param context spring application context
     */
    protected void afterStarted(ApplicationContext context) {
    }
}
