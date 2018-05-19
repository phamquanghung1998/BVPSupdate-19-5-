package vn.ithanh.udocter.model;

/**
 * Created by iThanh on 12/7/2017.
 */
public class BOOKING_INFO {

    private int id;
    private int patient_id;
    private String patient_name;
    private String patient_phone;
    private int docter_id;
    private String docter_name;
    private String docter_phone;
    private String comment;
    private String created;
    private String updated;
    private int status;
    private int tam_date;
    private int vs_date;
    private double longitude;
    private double latitude;
    private double rate;

    public String getPatient_phone() {
        return patient_phone;
    }

    public void setPatient_phone(String patient_phone) {
        this.patient_phone = patient_phone;
    }

    public String getDocter_phone() {
        return docter_phone;
    }

    public void setDocter_phone(String docter_phone) {
        this.docter_phone = docter_phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(int patient_id) {
        this.patient_id = patient_id;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public int getDocter_id() {
        return docter_id;
    }

    public void setDocter_id(int docter_id) {
        this.docter_id = docter_id;
    }

    public String getDocter_name() {
        return docter_name;
    }

    public void setDocter_name(String docter_name) {
        this.docter_name = docter_name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTam_date() {
        return tam_date;
    }

    public void setTam_date(int tam_date) {
        this.tam_date = tam_date;
    }

    public int getVs_date() {
        return vs_date;
    }

    public void setVs_date(int vs_date) {
        this.vs_date = vs_date;
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

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
