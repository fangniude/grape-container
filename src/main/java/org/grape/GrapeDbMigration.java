package org.grape;

import com.google.common.base.Strings;
import io.ebean.annotation.Platform;
import io.ebean.config.ClassLoadConfig;
import io.ebean.config.DbMigrationConfig;
import io.ebean.config.PropertiesWrapper;
import io.ebean.config.ServerConfig;
import io.ebeaninternal.dbmigration.DbOffline;
import io.ebeaninternal.dbmigration.DefaultDbMigration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class GrapeDbMigration extends DefaultDbMigration {
    public GrapeDbMigration() {
        BaseModel.enableEnhance();

        super.addPlatform(Platform.MYSQL, Platform.MYSQL.name().toLowerCase());
    }


    public void generate(String plgName, String version) throws IOException {
        generate(plgName, version, null);
    }

    public void generate(String plgName, String version, String pendingDropVersion) throws IOException {
        DbOffline.setGenerateMigration();

        this.serverConfig = new ServerConfig();
        this.serverConfig.addPackage(String.format("%s.domain", plgName));
        this.constraintNaming = serverConfig.getConstraintNaming();

        migrationConfig = new DbMigrationConfig();
        Properties properties = new Properties();
        properties.setProperty("migration.name", plgName);
        properties.setProperty("migration.version", version);
        properties.setProperty("migration.migrationPath", String.format("dbmigration/%s", plgName));
        if (!Strings.isNullOrEmpty(pendingDropVersion)) {
            properties.setProperty("migration.generatePendingDrop", pendingDropVersion);
        }
        migrationConfig.loadSettings(new PropertiesWrapper(properties, new ClassLoadConfig()), "db");

        super.generateMigration();
    }

    public void generate() throws IOException {
        System.setProperty("ebean.props.file", "sql_generator.properties");
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("sql_generator.properties")) {
            if (in == null) {
                throw new GrapeException("no config file sql_generator.properties.");
            }
        } catch (IOException e) {
            throw new GrapeException("no config file sql_generator.properties.", e);
        }
        super.generateMigration();
    }

}
