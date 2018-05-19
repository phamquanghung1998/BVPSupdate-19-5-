package vn.ithanh.udocter.model;

/**
 * Created by iThanh on 12/24/2017.
 */

public class PATIENT_INFO {
    private int id;
    private int user_id;
    private String name;
    private String bod;
    private int age;
    private int sex_id;
    private String info_p;
    private String address;
    private String phone;
    private double longitude;
    private double latitude;
    private int star;
    private String avartar;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBod() {
        return bod;
    }

    public void setBod(String bod) {
        this.bod = bod;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSex_id() {
        return sex_id;
    }

    public void setSex_id(int sex_id) {
        this.sex_id = sex_id;
    }

    public String getInfo_p() {
        return info_p;
    }

    public void setInfo_p(String info_p) {
        this.info_p = info_p;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public String getAvartar() {
        return avartar;
    }

    public void setAvartar(String avartar) {
        this.avartar = avartar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
