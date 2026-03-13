package gtanks.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ResourceUtils {
    private static final String DATA_FALLBACK_PATH = "src/main/resources/data";
    private static final String CONFIG_FALLBACK_PATH = "src/main/resources/config";
    private static final Path DATA_BASE_PATH = resolveBasePath("data", DATA_FALLBACK_PATH);
    private static final Path CONFIG_BASE_PATH = resolveBasePath("config", CONFIG_FALLBACK_PATH);

    private ResourceUtils() {
    }

    public static String data(String relativePath) {
        return normalize(DATA_BASE_PATH.resolve(relativePath));
    }

    public static String config(String relativePath) {
        return normalize(CONFIG_BASE_PATH.resolve(relativePath));
    }

    private static Path resolveBasePath(String classpathFolder, String fallbackPath) {
        URL resource = ResourceUtils.class.getClassLoader().getResource(classpathFolder);
        if (resource != null && "file".equalsIgnoreCase(resource.getProtocol())) {
            try {
                URI uri = resource.toURI();
                return Paths.get(uri).toAbsolutePath().normalize();
            } catch (URISyntaxException ignored) {
            }
        }
        return Paths.get(fallbackPath).toAbsolutePath().normalize();
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }
}
