package com.csu.demo.demo.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName(value = "items")
@Data
public class Item {
    @TableId(value = "id")
    private int id;
    private String catagory;
    private String tags_json;
    private String embedding_string;
}
