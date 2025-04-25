package org.dataoganalysekaeder.storeInfo;

import org.jsoup.nodes.Element;
import org.w3c.dom.ls.LSOutput;

import java.util.List;

public class Rema1000 extends StoreInfo {
    private String owner;


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
                        name = "Rema 1000 " + element.text();
                    }
                }
                case "address" -> {
                    if(address == null) {
                        address = element.text();
                    }
                }
                case "span" -> {
                    if(owner == null) {
                        owner = element.text();
                    }
                }
                case "div" -> {
                    if(phone == null && element.text().toLowerCase().contains("tlf")) {
                        int index = element.text().toLowerCase().lastIndexOf("tlf.:");
                        phone = element.text().substring(index);
                    }
                }
            }
        }

    }

    @Override
    public String getCSVRow() {
        return name+";"+address+";"+owner+";"+phone;
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
