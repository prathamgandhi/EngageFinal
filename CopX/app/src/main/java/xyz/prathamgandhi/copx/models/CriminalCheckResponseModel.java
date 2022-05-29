package xyz.prathamgandhi.copx.models;

public class CriminalCheckResponseModel {
   String first_name, last_name, age, crime;

    public CriminalCheckResponseModel(String first_name, String last_name, String age, String crime) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.age = age;
        this.crime = crime;
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

    public String getCrime() {
        return crime;
    }

    public void setCrime(String crime) {
        this.crime = crime;
    }
}
