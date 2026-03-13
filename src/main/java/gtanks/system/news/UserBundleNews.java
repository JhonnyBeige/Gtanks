/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.news;

import gtanks.system.news.objects.NewsObject;
import java.util.ArrayList;
import java.util.List;

public class UserBundleNews {
    private List<NewsObject> news = new ArrayList<NewsObject>();

    public void add(NewsObject obj) {
        this.news.add(obj);
    }

    public List<NewsObject> get() {
        return this.news;
    }
}

