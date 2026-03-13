/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.services;

import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.services.annotations.ServicesInject;
import gtanks.system.news.NewsRepository;
import gtanks.system.news.UserBundleNews;
import gtanks.system.news.objects.NewsObject;
import gtanks.system.news.objects.UserViewedNewsInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class NewsService {
    private static final NewsService instance = new NewsService();
    @ServicesInject(target=DatabaseManager.class)
    private static final DatabaseManager databaseManager = DatabaseManagerImpl.instance();
    private static final NewsRepository newsRepository = NewsRepository.instance();
    private Map<LobbyManager, UserBundleNews> waitingUsers = new HashMap<LobbyManager, UserBundleNews>();

    public static NewsService instance() {
        return instance;
    }

    private NewsService() {
    }

    public void userInited(LobbyManager lobby) {
        List<NewsObject> news = newsRepository.collectNews();
        ArrayList<Long> viewedNews = new ArrayList<Long>();
        UserBundleNews bundle = null;
        UserViewedNewsInfo userViewedNewsInfo = databaseManager.getUserViewedNews(lobby.getLocalUser().getNickname());
        if (userViewedNewsInfo == null) {
            userViewedNewsInfo = new UserViewedNewsInfo();
            userViewedNewsInfo.setNickname(lobby.getLocalUser().getNickname());
            userViewedNewsInfo.setIds("");
            databaseManager.register(userViewedNewsInfo);
        }
        this.parseViewedIds(userViewedNewsInfo.getIds(), viewedNews);
        ArrayList<Long> currentNewsIds = new ArrayList<Long>();
        for (NewsObject newsObj : news) {
            currentNewsIds.add(newsObj.getId());
            if (viewedNews.contains(newsObj.getId())) continue;
            if (bundle == null) {
                bundle = new UserBundleNews();
            }
            bundle.add(newsObj);
        }
        userViewedNewsInfo.setIds(this.stringflyViewedIds(currentNewsIds));
        databaseManager.update(userViewedNewsInfo);
        this.waitingUsers.put(lobby, bundle);
    }

    public void userLoaded(LobbyManager lobby) {
        UserBundleNews bundle = this.waitingUsers.get(lobby);
        if (bundle != null) {
            this.waitingUsers.remove(lobby);
            lobby.send(Type.LOBBY, "show_news", this.parseShowNewsCommand(bundle));
        }
    }

    private String parseShowNewsCommand(UserBundleNews bundle) {
        JSONArray news = new JSONArray();
        bundle.get().forEach(newsObject -> {
            JSONObject obj = new JSONObject();
            obj.put("date", newsObject.getDate());
            obj.put("icon_id", newsObject.getIconId());
            obj.put("text", newsObject.getText());
            news.add(obj);
        });
        return news.toJSONString();
    }

    private String stringflyViewedIds(List<Long> viewedNews) {
        JSONArray array = new JSONArray();
        viewedNews.forEach(id -> array.add(id));
        return array.toJSONString();
    }

    private void parseViewedIds(String str, List<Long> viewedNews) {
        if (str != null && !str.trim().isEmpty()) {
            JSONParser parser = new JSONParser();
            try {
                JSONArray array = (JSONArray)parser.parse(str);
                for (Object obj : array) {
                    viewedNews.add(((Number)obj).longValue());
                }
            } catch (ParseException var7) {
                var7.printStackTrace();
            }
        }
    }
}