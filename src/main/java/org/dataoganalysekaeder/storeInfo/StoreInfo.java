package org.dataoganalysekaeder.storeInfo;

import org.dataoganalysekaeder.CVRSearch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StoreInfo implements Comparable<StoreInfo> {
    protected String name;
    protected Address address;
    protected int pNummer;
    protected int CVRNum;
    protected String CVRName;


    public StoreInfo() {
        pNummer = -1;
        CVRNum = -1;
        CVRName = null;
    }

    public abstract String getCSVRow();

    @Override
    public int compareTo(StoreInfo o) {
        return name.compareTo(o.name);
    }

    public boolean hasNumericValue(String str) {
        return str.matches("\\d+");
    }

    public Address getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public void setPNummer(int pNummer) {
        this.pNummer = pNummer;
    }

    public void setCVRNum(int CVRNum) {
        this.CVRNum = CVRNum;
    }

    public void setCVRName(String CVRName) {
        this.CVRName = CVRName;
    }

    public void parseAddressHandleCVR(String addressString) {

        String[] split = addressString.split(",");
        if(split.length > 2 && !split[0].matches(".*\\d.*")) {
            addressString = split[1].replaceAll("(\\d)\\s+([A-Za-z])", "$1$2") + ", " + split[split.length - 1];
        } else {
            addressString = split[0].replaceAll("(\\d)\\s+([A-Za-z])", "$1$2") + ", " + split[split.length - 1];
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
}
