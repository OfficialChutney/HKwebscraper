package org.dataoganalysekaeder.scraper;

import org.dataoganalysekaeder.storeInfo.StoreInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XLBygScraper implements Scraper {


    @Override
    public void scrape() {
        Map<String, String> storesToURL = new HashMap<>();


        System.setProperty("webdriver.chrome.driver", "./driver/chromedriver.exe");

        // 2. Optional: run in headless mode
        ChromeOptions options = new ChromeOptions();
        options.addArguments("window-size=580,800"); // width x height
        // options.addArguments("--headless"); // Uncomment if you want no browser UI

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.xl-byg.dk/find-xl-byg");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Adjust timeout

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[id^='headlessui-combobox-option-']")));


            String renderedHtml = driver.getPageSource();
            driver.quit();

            System.out.println(renderedHtml.contains("headlessui-combobox-option-"));

            Document doc = Jsoup.parse(renderedHtml);
            Elements elements = doc.select(".FindStoreSearchInput_listView__PC1QI");

            boolean isName = true;
            for (Element element : elements) {

                for (Element spanElement : element.select("span")) {
                    if (isName) {
                        System.out.println("Name: " + spanElement.text().trim());
                        isName = false;
                    } else {
                        System.out.println("Address: " + spanElement.text().trim());
                        isName = true;
                        break;
                    }

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public String getCSVHeader() {
        return "";
    }

    @Override
    public String getChainName() {
        return "";
    }

    @Override
    public List<StoreInfo> getStores() {
        return List.of();
    }
}
