package org.dataoganalysekaeder;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MatasForsoergChromeDriver {
    public static void main(String[] args) throws Exception {

//        Controller controller = new Controller();
//        controller.scrape();
//        controller.printToCSV();

        System.setProperty("webdriver.chrome.driver", "./driver/chromedriver.exe");

        // 2. Optional: run in headless mode
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // Uncomment if you want no browser UI

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.matas.dk/find-butik?addressQuery=Ryesgade%2026,%203.%209,%202200%20K%C3%B8benhavn%20N");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(), 'Se flere afhentningssteder')]")));

            // 4. Inject your JavaScript to repeatedly click "Vis mere"
            String jsScript = """
                        const intervalId = setInterval(() => {
                            const buttons = Array.from(document.querySelectorAll('button'));
                            const target = buttons.find(b => b.textContent.includes('Se flere afhentningssteder'));
                            if (target && target.offsetParent !== null) {
                                target.click();
                            } else {
                                clearInterval(intervalId);
                                window.doneLoading = true;
                            }
                        }, 300);
                    """;
            ((JavascriptExecutor) driver).executeScript(jsScript);


            JavascriptExecutor js = (JavascriptExecutor) driver;

            js.executeScript(jsScript);

            WebDriverWait waitForJS = new WebDriverWait(driver, Duration.ofSeconds(30)); // Adjust timeout
            waitForJS.until(driver1 -> (Boolean) ((JavascriptExecutor) driver1)
                    .executeScript("return window.doneLoading === true"));

            // 6. Grab final rendered HTML and parse with JSoup
            String renderedHtml = driver.getPageSource();

            System.out.println(renderedHtml);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // Clean up
        }


    }

}
