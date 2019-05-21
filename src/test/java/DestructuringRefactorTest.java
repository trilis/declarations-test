import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DestructuringRefactorTest {

    @Test
    void testRefactor() throws IOException {
        String prefix = "src/test/test_files/";
        DestructuringRefactor.refactor(prefix + "ping");
        DestructuringRefactor.refactor(prefix + "order");
    }
}