package org.dataoganalysekaeder.storeInfo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public abstract class StoreInfo implements Comparable<StoreInfo> {
    protected String name;
    protected String address;
    protected String phone;
    public abstract String getCSVRow();


    @Override
    public int compareTo(StoreInfo o) {
        return name.compareTo(o.name);
    }
}
