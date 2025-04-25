package org.dataoganalysekaeder;

public class Main {

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.scrape();
        controller.printToCSV();
    }

}
