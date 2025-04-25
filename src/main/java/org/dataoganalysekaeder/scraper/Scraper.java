package org.dataoganalysekaeder.scraper;

import org.dataoganalysekaeder.storeInfo.StoreInfo;

import java.util.List;

public interface Scraper {


    void scrape();
    String getCSVHeader();
    String getChainName();
    List<StoreInfo> getStores();

}
