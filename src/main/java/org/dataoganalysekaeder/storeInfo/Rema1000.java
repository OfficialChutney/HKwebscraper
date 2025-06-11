package org.dataoganalysekaeder.storeInfo;

import org.dataoganalysekaeder.CVRSearch;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rema1000 extends StoreInfo {
    private String owner;
    private String phone;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }


    public void addItem(List<Element> elements) {
        for (Element element : elements) {
            switch (element.tagName()) {
                case "h2" -> {
                    if(name == null) {
                        name = "Rema 1000 " + element.text().trim();
                    }
                }
                case "address" -> {
                    if(address == null) {
                        parseAddressHandleCVR(element.text().trim());
                    }
                }
                case "span" -> {
                    if(owner == null) {
                        owner = element.text().substring(8).trim();
                    }
                }
                case "div" -> {
                    if(phone == null && element.text().toLowerCase().contains("tlf")) {
                        phone = element.text().replace("Tlf.:","").trim();
                    }
                }
            }
        }
    }



    @Override
    public String getCSVRow() {
        //PNUMMER;CVRNUM;CVRNAME;STORE_NAME;ADDRESS;OWNER;PHONE;
        return pNummer +";" + CVRNum + ";" + CVRName + ";" + name + ";" + address + ";" + owner + ";" + phone;
    }

    @Override
    public String toString() {
        return "Rema1000{" +
                "owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
