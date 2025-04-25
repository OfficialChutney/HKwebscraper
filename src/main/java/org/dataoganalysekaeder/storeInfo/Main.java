package org.dataoganalysekaeder.storeInfo;

import org.dataoganalysekaeder.Controller;

public class Main {

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.scrape();
        controller.printToCSV();
    }

}
