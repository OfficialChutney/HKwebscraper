package org.dataoganalysekaeder;

import org.dataoganalysekaeder.scraper.Rema1000Scraper;
import org.dataoganalysekaeder.scraper.Scraper;
import org.dataoganalysekaeder.storeInfo.StoreInfo;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Controller {

    private List<Scraper> scrapers;


    public Controller() {
        scrapers = new LinkedList<Scraper>();
        scrapers.add(new Rema1000Scraper());
    }


    public void scrape() {
        for (Scraper scraper : scrapers) {
            scraper.scrape();
        }
    }

    public void printToCSV() {

        String dirPath = ("."+File.separator+"data");
        File dir = new File(dirPath);

        if(!dir.exists()) {
            boolean createdDir = dir.mkdirs();

            if(!createdDir) {
                throw new RuntimeException("Unable to create data directory");
            }
        }
        for (Scraper scraper : scrapers) {
            File file = new File(dirPath+File.separator+scraper.getChainName()+".csv");

            if(!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Unable to create file");
                }
            }
            try {
                PrintWriter writer = new PrintWriter(file);

                writer.println(scraper.getCSVHeader());

                System.out.println("Num of stores" + scraper.getStores().size());

                for (StoreInfo storeInfo : scraper.getStores()) {
                    writer.println(storeInfo.getCSVRow());
                }

                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
