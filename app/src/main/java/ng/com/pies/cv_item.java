package ng.com.pies;

/**
 * Created by Nsikak  Thompson on 10/22/2016.
 */
public class cv_item {


    public  cv_item(){

    }
    public cv_item(String name, String email, String phone, String cv_url) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.cv_url = cv_url;
    }

    private String name, email, phone, cv_url;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCv_url() {
        return cv_url;
    }

    public void setCv_url(String cv_url) {
        this.cv_url = cv_url;
    }


}
