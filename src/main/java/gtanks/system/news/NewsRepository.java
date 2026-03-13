package gtanks.system.news;

import gtanks.system.news.objects.NewsObject;
import gtanks.utils.ResourceUtils;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NewsRepository {
    private static final NewsRepository instance = new NewsRepository();
    private final Path newsPath = Paths.get(ResourceUtils.data("json/news.json"));
    private volatile long cachedLastModified = -1L;
    private volatile List<NewsObject> cachedNews = Collections.emptyList();

    public static NewsRepository instance() {
        return instance;
    }

    private NewsRepository() {
    }

    public List<NewsObject> collectNews() {
        try {
            if (!Files.exists(this.newsPath)) {
                return Collections.emptyList();
            }
            long lastModified = Files.getLastModifiedTime(this.newsPath).toMillis();
            if (this.cachedLastModified == lastModified) {
                return new ArrayList<NewsObject>(this.cachedNews);
            }
            List<NewsObject> loadedNews = this.loadNews();
            this.cachedNews = loadedNews;
            this.cachedLastModified = lastModified;
            return new ArrayList<NewsObject>(loadedNews);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<NewsObject>(this.cachedNews);
        }
    }

    private List<NewsObject> loadNews() {
        ArrayList<NewsObject> result = new ArrayList<NewsObject>();
        JSONParser parser = new JSONParser();
        try (BufferedReader reader = Files.newBufferedReader(this.newsPath, StandardCharsets.UTF_8)) {
            JSONArray array = (JSONArray)parser.parse(reader);
            for (int index = 0; index < array.size(); ++index) {
                JSONObject obj = (JSONObject)array.get(index);
                NewsObject newsObject = new NewsObject();
                String date = this.normalizeString(obj.get("date"));
                String text = this.normalizeString(obj.get("text"));
                String iconId = this.normalizeString(obj.get("icon_id"));
                newsObject.setDate(date);
                newsObject.setText(text);
                newsObject.setIconId(iconId);
                newsObject.setId(this.createStableNewsId(index, date, text, iconId));
                result.add(newsObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String normalizeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private long createStableNewsId(int index, String date, String text, String iconId) {
        CRC32 crc32 = new CRC32();
        crc32.update((index + "|" + date + "|" + text + "|" + iconId).getBytes(StandardCharsets.UTF_8));
        return crc32.getValue();
    }
}