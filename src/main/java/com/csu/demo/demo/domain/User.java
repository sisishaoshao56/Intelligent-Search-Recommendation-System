package com.csu.demo.demo.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName(value = "users")
@Data
public class User {
    @TableId(value = "id")
    private int id;
    private String username;
    private String password;
    private int age;
    private Boolean gender;
    private String tags_json;

}
