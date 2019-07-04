package org.grape;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.typesafe.config.*;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import io.ebeaninternal.server.core.DefaultServer;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * application starter
 *
 * @author lewis
 */
@Slf4j
@NoArgsConstructor
@EnableAutoConfiguration
@SpringBootApplication
public class GrapeApplication {

    private static final Config CONFIG = ConfigFactory.load();
    private static final Properties PROPERTIES = loadProperties();

    private static final ImmutableMap<String, Plugin> PLUGINS = loadPlugins();

    static final String DATA_SOURCE_DEFAULT = "default";
    private static final String ENABLE_PLUGIN_ALL = "all";

    private static ApplicationContext appContext;

    static {
        BaseModel.enableEnhance();
        loadDataSources();
    }

    public static void main(String[] args) {
        // 0. in the beginning
        PLUGINS.values().forEach(Plugin::inTheBeginning);

        // 1. db migration
        if (PLUGINS.values().stream().anyMatch(Plugin::hasEntity)) {
            dbMigration();
            PLUGINS.values().forEach(Plugin::afterDataBaseMigration);
        }

        // 2. start spring boot
        initSpring();

        // start success
        log.info("\r\n*******************************************\r\n"
                + "#######		STARTUP SUCCESS\r\n" //
                + "#######		http://localhost:" + getConfig("server.port") + "\r\n"
                + "*******************************************\r\n");


        // 3. after started
        PLUGINS.values().forEach(plg -> plg.afterStarted(appContext));
    }

    private static void dbMigration() {
        PLUGINS.values().stream().filter(Plugin::hasEntity).map(Plugin::name).forEach(name -> {
            DefaultServer es = (DefaultServer) getPlugin(name).ebeanServer();

            String platformName = es.getDatabasePlatform().getName();

            MigrationConfig mc = new MigrationConfig();
            mc.setPlatformName(platformName);
            mc.setMetaTable(String.format("%s_db_migration", name));
            mc.setMigrationPath(String.format("dbmigration/%s/%s/", name, platformName));

            MigrationRunner runner = new MigrationRunner(mc);
            runner.run(es.getDataSource());
        });
    }

    private static void initSpring() {
        log.info("Init spring begin.");

        SpringApplication app = new SpringApplication(GrapeApplication.class);

        // set spring scan packages
        app.setSources(PLUGINS.keySet());

        // set dubbo scan packages
        PROPERTIES.setProperty("dubbo.scan.base-packages", String.join(",", PLUGINS.keySet()));
        app.setDefaultProperties(PROPERTIES);

        // init spring
        log.info("Loading spring beans, this will take some time, please be patient.");
        appContext = app.run();
        log.info("Init spring end.\n");
    }


    /**
     * Read config in application.properties
     *
     * @param name the property namen
     * @return the property value
     */
    @NonNull
    private static String getConfig(@NonNull String name) {
        String value = PROPERTIES.getProperty(name);
        if (Strings.isNullOrEmpty(value)) {
            String msg = String.format("%s must config in application.properties.", name);
            log.error("\r\n********************************************************************\r\n"
                    + "#######		Missing Config  \r\n"
                    + "#######		" + msg + "\r\n"
                    + "********************************************************************\r\n");
            throw new GrapeException(msg);
        }
        return value;
    }

    private static Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream is = GrapeApplication.class.getResourceAsStream("/application.properties")) {
            p.load(is);
            return p;
        } catch (IOException e) {
            throw new GrapeException("load application.properties error.", e);
        }
    }

    @NonNull
    private static ImmutableMap<String, Plugin> loadPlugins() {
        log.info("Load grape plugin begin.");
        Map<String, Plugin> map = Maps.newHashMap();

        ServiceLoader<Plugin> grapes = ServiceLoader.load(Plugin.class);

        ConfigObject dsMap = CONFIG.getConfig("datasource").root();

        for (Plugin grape : grapes) {
            String name = grape.name();
            log.info("find grape plugin: " + name);

            if (map.containsKey(name)) {
                String msg = String.format("Duplicate plugins: %s, plugin name must be unique.", name);
                log.error(msg);
                throw new GrapeException(msg);
            }

            ConfigValue cv = dsMap.get(name);
            if (cv != null && ConfigValueType.STRING.equals(cv.valueType())) {
                grape.setDbName(String.valueOf(cv.unwrapped()));
                log.info("set the data source of plugin [{}] to [{}].", name, cv.unwrapped());
            }

            map.put(name, grape);
        }

        log.info("loaded all plugins: [{}]", String.join(", ", map.keySet()));

        String enableGrapes = getConfig("plugins.enable");

        if (Strings.isNullOrEmpty(enableGrapes) || ENABLE_PLUGIN_ALL.equalsIgnoreCase(enableGrapes)) {
            log.info("all plugins enabled.\n");

            return ImmutableMap.copyOf(map);
        } else {
            HashSet<String> configSet = Sets.newHashSet(enableGrapes.split(","));

            Sets.SetView<String> configGrapesNotFoundSet = Sets.difference(configSet, map.keySet());
            if (!configGrapesNotFoundSet.isEmpty()) {
                log.warn("config plugins: [{}] not found.", String.join(", ", configGrapesNotFoundSet));
            }

            Sets.SetView<String> ignoreGrapesSet = Sets.difference(map.keySet(), configSet);
            if (!ignoreGrapesSet.isEmpty()) {
                log.warn("ignore plugins: [{}].", String.join(", ", ignoreGrapesSet));
            }

            Sets.SetView<String> enableGrapesSet = Sets.intersection(configSet, map.keySet());
            if (!enableGrapesSet.isEmpty()) {
                log.info("enable plugins: [{}].", String.join(", ", enableGrapesSet));
            } else {
                log.warn("no plugin enabled.");
            }

            Map<String, Plugin> enableMap = map.entrySet().stream().filter(e -> enableGrapesSet.contains(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return ImmutableMap.copyOf(enableMap);
        }
    }

    private static void loadDataSources() {
        Map<String, ServerConfig> serverMap = loadDataSourcesConfig();

        Set<String> hasEntityGrapes = PLUGINS.values().stream().filter(Plugin::hasEntity).map(Plugin::name).collect(Collectors.toSet());
        log.info("has entity plugins: [{}]", String.join(", ", hasEntityGrapes));

        hasEntityGrapes.stream()
                .map(name -> {
                    ServerConfig sc = serverMap.get(getPlugin(name).getDbName());

                    if (sc != null) {
                        sc.addPackage(String.format("%s.domain", name));
                    } else {
                        throw new GrapeException(String.format("datasource for grape %s not config and the default datasource not exist.", name));
                    }
                    return sc;
                }).distinct()
                .forEach(sc -> {
                    log.info("data source [{}] support packages list: [{}].", sc.getName(), String.join(", ", sc.getPackages()));

                    EbeanServer es = EbeanServerFactory.create(sc);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> es.shutdown(true, true)));
                });
    }

    @NonNull
    private static Map<String, ServerConfig> loadDataSourcesConfig() {
        ConfigObject dsMap = CONFIG.getConfig("datasource").root();

        Map<String, ServerConfig> serverMap = Maps.newHashMap();
        for (Map.Entry<String, ConfigValue> entry : dsMap.entrySet()) {
            String k = entry.getKey();
            ConfigValue v = entry.getValue();

            if (v.valueType() == ConfigValueType.OBJECT) {
                serverMap.put(k, serverConfig(k));
            }
        }

        log.info("all datasources: [{}]", String.join(", ", serverMap.keySet()));
        return ImmutableMap.copyOf(serverMap);
    }

    private static ServerConfig serverConfig(String name) {
        ServerConfig sc = new ServerConfig();
        sc.setName(name);

        if (DATA_SOURCE_DEFAULT.equalsIgnoreCase(name)) {
            sc.setDefaultServer(true);
        }

        sc.loadFromProperties(GrapeApplication.PROPERTIES);
        sc.setRegister(true);
        sc.setLazyLoadBatchSize(100);

        sc.addPackage(BaseDomain.class.getPackage().getName());
        return sc;
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public static Plugin getPlugin(@NonNull String pluginName) {
        Preconditions.checkArgument(PLUGINS.containsKey(pluginName), String.format("plugin [%s] not exist.", pluginName));
        return PLUGINS.get(pluginName);
    }

    @NonNull
    public static <T> T getSpringBean(Class<T> tClass) {
        return appContext.getBean(tClass);
    }
}
