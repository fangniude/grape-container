package org.grape;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.avaje.agentloader.AgentLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * application starter
 *
 * @author lewis
 */
@Slf4j
@NoArgsConstructor
@SpringBootApplication
public class GrapeApplication {

    private static final Config CONFIG = ConfigFactory.load();
    private static final Properties PROPERTIES = loadProperties();

    private static final ImmutableMap<String, Grape> GRAPES = loadGrapes();

    private static final Map<String, String> DATASOURCE_REFERENCE_MAP = loadDataSourceReferences();
    private static final ImmutableMap<String, EbeanServer> SERVER_MAP = loadDataSources();

    private static ApplicationContext appContext;

    static {
        // set ebean auto enhance
        if (!AgentLoader.loadAgentFromClasspath("ebean-agent", "debug=1")) {
            log.info("ebean-agent not found in classpath - not dynamically loaded");
        }
    }

    public static void main(String[] args) {
        // 0. in the beginning
        GRAPES.values().forEach(Grape::inTheBeginning);

        // 1. db migration
        if (GRAPES.values().stream().anyMatch(Grape::hasEntity)) {
//            flyway();
//            dbMigration();
            GRAPES.values().forEach(Grape::afterDataBaseMigration);
        }

        // 2. start spring boot
        initSpring(args);

        // start success
        log.info("\r\n*******************************************\r\n" //
                + "#######		STARTUP SUCCESS\r\n" //
                + "#######		http://localhost:" + getConfig("server.port") + "\r\n"//
                + "*******************************************\r\n");


        // 3. after started
        GRAPES.values().forEach(plg -> plg.afterStarted(appContext));
    }

    private static void dbMigration() {
        GRAPES.values().stream().filter(Grape::hasEntity).map(Grape::name).forEach(name -> {
            DefaultServer es = (DefaultServer) grapeEbeanServer(name);

            String platformName = es.getDatabasePlatform().getName();

            MigrationConfig mc = new MigrationConfig();
            mc.setPlatformName(platformName);
            mc.setMetaTable(String.format("%s_db_migration", name));
            mc.setMigrationPath(String.format("sql/%s/%s/", name, platformName));

            MigrationRunner runner = new MigrationRunner(mc);
            runner.run(es.getDataSource());
        });
    }

    @NonNull
    private static EbeanServer grapeEbeanServer(String name) {
        Grape grape = GRAPES.get(name);
        if (grape != null) {
            if (grape.hasEntity()) {
                return SERVER_MAP.getOrDefault(name, SERVER_MAP.get(DATASOURCE_REFERENCE_MAP.getOrDefault(name, "default")));
            } else {
                throw new GrapeException(String.format("grape: %s not has entity, unsupported this method", name));
            }
        } else {
            throw new GrapeException(String.format("grape: %s not exist", name));
        }
    }

    private static EbeanServer ebeanServer(ServerConfig sc) {
        EbeanServer es = EbeanServerFactory.create(sc);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                es.shutdown(true, true);
            }
        });
        return es;
    }

    private static ServerConfig serverConfig(String name) {
        ServerConfig sc = new ServerConfig();
        sc.setName(name);

        if ("default".equalsIgnoreCase(name)) {
            sc.setDefaultServer(true);
        }

        sc.loadFromProperties(GrapeApplication.PROPERTIES);
        sc.setRegister(true);
        sc.setLazyLoadBatchSize(100);

        sc.addPackage(BaseGrapeModel.class.getPackage().getName());
        return sc;
    }

    private static void initSpring(String[] args) {
        log.info("Init spring begin.");
        ArrayList<Object> ss = Lists.newArrayList(GrapeApplication.class);
        ss.addAll(GRAPES.keySet().stream().map(Package::getPackage).collect(Collectors.toList()));
        // todo all packaged need add
        SpringApplication app = new SpringApplication(GrapeApplication.class);

        // init spring
        log.info("Loading spring beans, this will take some time, please be patient.");
        appContext = app.run(args);
        log.info("Init spring end.\n");
    }


    /**
     * Read config in application.properties
     *
     * @param name the property namen
     * @return the property value
     */
    @NonNull
    public static String getConfig(String name) {
        String value = PROPERTIES.getProperty(name);
        if (Strings.isNullOrEmpty(value)) {
            String msg = String.format("%s must config in application.properties.", name);
            log.error("\r\n********************************************************************\r\n" //
                    + "#######		Missing Config  \r\n" //
                    + "#######		" + msg + "\r\n"//
                    + "********************************************************************\r\n");
            throw new GrapeException(msg);
        }
        return value;
    }

    public static String getProperty(String name, String defaultValue) {
        return PROPERTIES.getProperty(name, defaultValue);
    }

    public static Properties getProperties() {
        return PROPERTIES;
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
    private static ImmutableMap<String, Grape> loadGrapes() {
        log.info("Load grape plugin begin.");
        Map<String, Grape> map = Maps.newHashMap();

        ServiceLoader<Grape> grapes = ServiceLoader.load(Grape.class);
        for (Grape grape : grapes) {
            String name = grape.name();
            log.info("find grape plugin: " + name);

            if (map.containsKey(name)) {
                String msg = String.format("Duplicate grapes: %s, grape name must be unique.", name);
                log.error(msg);
                throw new GrapeException(msg);
            }

            map.put(name, grape);
        }

        log.info("Load grape plugin end.\n");

        return ImmutableMap.copyOf(map);
    }

    private static ImmutableMap<String, EbeanServer> loadDataSources() {
        Map<String, ServerConfig> serverMap = loadDataSourcesConfig();

        Set<String> hasEntityGrapes = GRAPES.values().stream().filter(Grape::hasEntity).map(Grape::name).collect(Collectors.toSet());

        Map<String, EbeanServer> map = hasEntityGrapes.stream()
                .map(name -> {
                    ServerConfig defaultConfig = serverMap.getOrDefault("default", serverMap.get(DATASOURCE_REFERENCE_MAP.get("default")));
                    ServerConfig sc = serverMap.getOrDefault(name, defaultConfig);

                    if (sc != null) {
                        sc.addPackage(name);
                    } else {
                        throw new GrapeException(String.format("datasource for grape %s not config and the default datasource not exist.", name));
                    }
                    return sc;
                }).distinct()
                .map(GrapeApplication::ebeanServer)
                .collect(Collectors.toMap(EbeanServer::getName, Function.identity()));

        return ImmutableMap.copyOf(map);
    }

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

        log.info("all datasources: {}", String.join(", ", serverMap.keySet()));
        return ImmutableMap.copyOf(serverMap);
    }

    private static Map<String, String> loadDataSourceReferences() {
        ConfigObject dsMap = CONFIG.getConfig("datasource").root();

        Map<String, String> datasourceReferenceMap = Maps.newHashMap();
        for (Map.Entry<String, ConfigValue> entry : dsMap.entrySet()) {
            String k = entry.getKey();
            ConfigValue v = entry.getValue();

            if (ConfigValueType.STRING.equals(v.valueType())) {
                datasourceReferenceMap.put(k, String.valueOf(v.unwrapped()));
            }
        }

        datasourceReferenceMap.forEach((k, v) -> {
            log.info("datasource reference map: {} -> {}", k, v);
        });

        return ImmutableMap.copyOf(datasourceReferenceMap);
    }
}
