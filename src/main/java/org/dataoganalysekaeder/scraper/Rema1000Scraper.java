package org.dataoganalysekaeder.scraper;

import org.dataoganalysekaeder.storeInfo.Rema1000;
import org.dataoganalysekaeder.storeInfo.StoreInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Rema1000Scraper implements Scraper {
    private final URL url;
    private final String IDENTIFIER;
    private final List<StoreInfo> stores;
    private final String CSVHEADER = "PNUMMER;CVRNUM;CVRNAME;STORE_NAME;ADDRESS;OWNER;PHONE;";
    private final String CHAIN = "Rema1000";

    public Rema1000Scraper() {
        stores = new LinkedList<>();
        IDENTIFIER = "[id^=store-list__item]";
        try {
            this.url = new URL("https://rema1000.dk/find-butik-og-abningstider");
        } catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void scrape() {
        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            InputStream input = connection.getInputStream();
            Document doc = Jsoup.parse(input, "UTF-8", url.toString());

            Elements elements = doc.select(IDENTIFIER);
            for (Element element : elements) {
                Rema1000 store = new Rema1000();
                store.addItem(collectLeafTextLines(element));
                stores.add(store);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Element> collectLeafTextLines(Element element, List<Element> lines) {
        if (element.children().isEmpty()) {
            String text = element.ownText().trim();
            if (!text.isEmpty()) lines.add(element);
        } else {
            for (Element child : element.children()) {
                collectLeafTextLines(child, lines);
            }
        }
        return lines;
    }

    public List<Element> collectLeafTextLines(Element element) {
        List<Element> lines = new LinkedList<>();
        return collectLeafTextLines(element, lines);
    }

    @Override
    public String getCSVHeader() {
        return CSVHEADER;
    }

    @Override
    public String getChainName() {
        return CHAIN;
    }

    @Override
    public List<StoreInfo> getStores() {
        stores.sort(Comparator.naturalOrder());
        return stores;
    }
}
