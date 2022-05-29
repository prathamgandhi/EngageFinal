package xyz.prathamgandhi.copx.models;

public class LostCheckResponseModel {
    String first_name, last_name, age, last_seen;

    public LostCheckResponseModel(String first_name, String last_name, String age, String last_seen) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.age = age;
        this.last_seen = last_seen;
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(String last_seen) {
        this.last_seen = last_seen;
    }
}
