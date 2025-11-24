import com.nurujjamanpollob.textenginejava.rag.config.RagConfig;
import com.nurujjamanpollob.textenginejava.rag.utils.PathValidator;
import com.nurujjamanpollob.textenginejava.rag.exception.RagException;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

public class RagSystemTest {

    @Test
    public void testConfigurationDefaults() {
        RagConfig config = RagConfig.getInstance();
        assertTrue(config.getChunkSize() > 0, "Chunk size should be positive");
        assertTrue(config.getMaxFileSize() > 0, "Max file size should be positive");
    }

    @Test
    public void testPathValidation_Valid() {
        assertDoesNotThrow(() -> {
            PathValidator.validatePath("src/main/java");
        });
    }

    @Test
    public void testPathValidation_Traversal() {
        // Should throw RagException or SecurityException
        Exception exception = assertThrows(Exception.class, () -> {
            PathValidator.validatePath("../../../etc/passwd");
        });
        assertTrue(exception.getMessage().contains("Invalid") || exception.getMessage().contains("suspicious"));
    }

    @Test
    public void testPathNormalization() throws RagException {
        // Base path
        java.nio.file.Path base = Paths.get("/tmp/project");
        // User input trying to break out
        String malicious = "../secret";

        // This simulates strict checking if we implemented the check inside validateAndNormalizePath
        // Since we are mocking the FS environment, we just check logic flow
        assertThrows(RagException.class, () -> {
            PathValidator.validateAndNormalizePath(malicious, base);
        });
    }
}