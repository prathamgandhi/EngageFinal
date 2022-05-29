package xyz.prathamgandhi.copx.models;

public class MarkAttendanceModel {
    String imageBase64;

    public MarkAttendanceModel(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
