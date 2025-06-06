package org.dataoganalysekaeder.storeInfo;

import org.dataoganalysekaeder.CVRSearch;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private void parseAddressHandleCVR(String addressString) {

        String[] split = addressString.split(",");
        if(split.length > 2 && !split[0].matches(".*\\d.*")) {
            addressString = split[1] + ", " + split[split.length - 1];
        } else {
            addressString = split[0] + ", " + split[split.length - 1];
        }


        Pattern p = Pattern.compile("\\b(\\d+-\\d+)\\b");
        Matcher m = p.matcher(addressString);
        int[] houseNumberRange = null;

        if(m.find()) {
            String range = m.group(1);
            String[] rangeArray = range.split("-");
            int firstNum = Integer.parseInt(rangeArray[0]);
            int lastNum  = Integer.parseInt(rangeArray[1]);

            if ((lastNum - firstNum) % 2 != 0) {
                lastNum += 1;
            }

            int count = ((lastNum - firstNum) / 2) + 1;

            houseNumberRange = new int[count];

            for (int j = 0; j < count; j++) {
                houseNumberRange[j] = firstNum + (2 * j);
            }

        }

        if(houseNumberRange != null) {
            for (int j : houseNumberRange) {
                this.address = new Address(addressString.replaceFirst("\\b\\d+-\\d+\\b", Integer.toString(j)));

                if(address.getHouseNumber() != j) {
                    continue;
                }

                CVRSearch.parse(this);

                if (pNummer != -1) {
                    break;
                }
            }
        } else {
            this.address = new Address(addressString);
            CVRSearch.parse(this);
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
