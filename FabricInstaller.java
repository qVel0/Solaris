import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricInstaller {
    public static String install(String mcVersion, Path gameDir) throws Exception {
        String resource = "/fabric/" + mcVersion + ".json";
        try (InputStream in = FabricInstaller.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("No bundled Fabric profile for " + mcVersion);
            }
            String profile = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            String id = JsonParser.parseString(profile).getAsJsonObject().get("id").getAsString();
            Path dir = gameDir.resolve("versions").resolve(id);
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(id + ".json"), profile);
            return id;
        }
    }
}