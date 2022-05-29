package xyz.prathamgandhi.copx.models;

public class CriminalRegisterModel {
    String first_name, last_name, crime, age, imageBase64;

    public CriminalRegisterModel(String first_name, String last_name, String crime, String age, String imageBase64) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.crime = crime;
        this.age = age;
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

    public String getCrime() {
        return crime;
    }

    public void setCrime(String crime) {
        this.crime = crime;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
