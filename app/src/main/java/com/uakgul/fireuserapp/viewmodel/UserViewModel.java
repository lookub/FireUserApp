package com.uakgul.fireuserapp.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.uakgul.fireuserapp.model.User;

import java.io.Serializable;

public class UserViewModel extends ViewModel implements Serializable {


    //model object
    private User userModel;

    private String username;
    private String password;
    private String nameSurname;
    private String age;
    private String city;
    private String image;

    public UserViewModel() {
        userModel = new User();
    }

    public UserViewModel(String username, String password, String nameSurname, String age, String city, String image) {
        this.username = username;
        this.password = password;
        this.nameSurname = nameSurname;
        this.age = age;
        this.city = city;
        this.image = image;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", nameSurname='" + nameSurname + '\'' +
                ", age='" + age + '\'' +
                ", city='" + city + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNameSurname() {
        return nameSurname;
    }

    public void setNameSurname(String nameSurname) {
        this.nameSurname = nameSurname;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

