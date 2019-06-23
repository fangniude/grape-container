package grape1;

import org.grape.GrapeDbMigration;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DbMigrationTest {

    @Test
    public void test() throws IOException {
        GrapeDbMigration dbm = new GrapeDbMigration();
        dbm.generate("grape1", "1.0.1");
    }
}
