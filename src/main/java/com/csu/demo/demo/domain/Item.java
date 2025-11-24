package com.csu.demo.demo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName(value = "items")
@Data
public class Item {
    @TableId(value = "id")
    private int id;
    private String title;
    @TableField("tags_json") 
    private String tagsJson;
    private String path;
    @TableField("embedding_vector") 
    private String embeddingVector;
    @TableField(exist = false)
    private String thumbPath;
}
