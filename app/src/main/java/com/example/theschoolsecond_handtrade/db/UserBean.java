package com.example.theschoolsecond_handtrade.db;

/**
 * 用户实体类，对应数据库 user 表。
 *
 * 表结构：
 *   id       INTEGER PRIMARY KEY AUTOINCREMENT
 *   username TEXT UNIQUE    用户名（唯一，不能重复）
 *   password TEXT           密码
 */
public class UserBean {

    private int id;
    private String username;
    private String password;

    public UserBean() {
    }

    public UserBean(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserBean(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
