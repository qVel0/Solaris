import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FabricInstaller {
    private static final String BASE = "https://raw.githubusercontent.com/YOUR_USER/YOUR_REPO/main/fabric/";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static String getText(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + res.statusCode() + " for " + url);
        }
        return res.body();
    }

    private static void download(String url, Path out) throws Exception {
        Files.createDirectories(out.getParent());
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<InputStream> res = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + res.statusCode() + " for " + url);
        }
        try (InputStream in = res.body()) {
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String install(String mcVersion, Path gameDir) throws Exception {
        String base = BASE + mcVersion + "/";
        String profile = getText(base + "profile.json");
        String id = JsonParser.parseString(profile).getAsJsonObject().get("id").getAsString();

        Path dir = gameDir.resolve("versions").resolve(id);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(id + ".json"), profile);

        for (String line : getText(base + "libraries.txt").split("\\R")) {
            String rel = line.trim();
            if (rel.isEmpty()) {
                continue;
            }
            Path out = gameDir.resolve("libraries").resolve(rel);
            if (!Files.exists(out)) {
                download(base + "libraries/" + rel, out);
            }
        }
        return id;
    }
}