package grape1;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import org.avaje.agentloader.AgentLoader;
import org.grape.GrapeDbMigration;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DbMigrationTest {
//    static {
//        AgentLoader.loadAgentFromClasspath("ebean-agent", "debug=1");
//    }

    @Test
    public void test() throws IOException {
        GrapeDbMigration dbm = new GrapeDbMigration();
        dbm.generate("grape1", "1.0.1");
    }
}
