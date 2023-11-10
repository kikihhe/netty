package com.xiaohe.test;

import java.io.Serializable;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-07 21:57
 */
public class User implements Serializable {
    private Integer id;

    private String name;

    private Integer age;

    private Integer sex;

    private Integer bodyWeight;

    private Integer height;

    private String school;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getBodyWeight() {
        return bodyWeight;
    }

    public void setBodyWeight(Integer bodyWeight) {
        this.bodyWeight = bodyWeight;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }
}
