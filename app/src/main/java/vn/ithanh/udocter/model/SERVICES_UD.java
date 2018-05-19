package vn.ithanh.udocter.model;

/**
 * Created by iThanh on 12/6/2017.
 */

public class SERVICES_UD {
    private int docter_id;
    private int service_id;
    private int price;
    private String name;

    public int getDocter_id() {
        return docter_id;
    }

    public void setDocter_id(int docter_id) {
        this.docter_id = docter_id;
    }

    public int getService_id() {
        return service_id;
    }

    public void setService_id(int service_id) {
        this.service_id = service_id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
