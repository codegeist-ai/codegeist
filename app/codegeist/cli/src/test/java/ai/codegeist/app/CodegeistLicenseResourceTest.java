package ai.codegeist.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Verifies that Maven copies the canonical repository license into the JAR resource tree. */
class CodegeistLicenseResourceTest {

    private static final Path ROOT_LICENSE = Path.of("..", "..", "..", "LICENSE").toAbsolutePath().normalize();
    private static final Path PACKAGED_LICENSE = Path.of("target", "classes", "META-INF", "LICENSE");

    @Test
    void packagedLicenseResourceMatchesRootLicense() throws IOException {
        assertThat(PACKAGED_LICENSE).isRegularFile();
        assertThat(Files.readString(PACKAGED_LICENSE)).isEqualTo(Files.readString(ROOT_LICENSE));
    }
}
