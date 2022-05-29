package xyz.prathamgandhi.copx.models;

public class LoginModel {

    private String imageBase64, first_name, last_name, password, phone;

    public LoginModel(String imageBase64, String first_name, String last_name, String password, String phone) {
        this.imageBase64 = imageBase64;
        this.first_name = first_name;
        this.last_name = last_name;
        this.password = password;
        this.phone = phone;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
