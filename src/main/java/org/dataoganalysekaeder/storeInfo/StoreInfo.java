package org.dataoganalysekaeder.storeInfo;

public abstract class StoreInfo implements Comparable<StoreInfo> {
    protected String name;
    protected Address address;
    protected String phone;
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
}
