package model;

import com.beust.jcommander.Parameter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {

    private String name;

    private String surname;

    private String number;

    private int age;

    private String polis;

    private String illness;

    private String first;

    private String last;

    private String doctor;

    private String time;

    public User(String name, String surname, String number, int age, String polis) {
        this.name = name;
        this.surname = surname;
        this.number = number;
        this.age = age;
        this.polis = polis;
    }
    public User() {}

    public User(String name, String surname, String illness, String first, String last) {
        this.name = name;
        this.surname = surname;
        this.illness = illness;
        this.first = first;
        this.last = last;
    }
    public User(String name, String surname, String number, int age, String polis, String doctor, String time) {
        this.name = name;
        this.surname = surname;
        this.number = number;
        this.age = age;
        this.polis = polis;
        this.doctor = doctor;
        this.time = time;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPolis() {
        return polis;
    }

    public void setPolis(String polis) {
        this.polis = polis;
    }

    public String getIllness() {
        return illness;
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    public void setIllness(String illness) {
        this.illness = illness;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Map<String, Object> getParameters() {
        Map<String, Object> map = new HashMap();
        Field[] fields = User.class.getDeclaredFields();
        try {
            for (Field f :
                    fields) {
                Object v = f.get(this);
                map.put(f.getName(), v);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException();
        }
        return map;
    }
}
