package com.csu.demo.demo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import lombok.Data;

@TableName(value = "items")
@Data
public class Item {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String title;
    @TableField("tags_json") 
    private String tagsJson;
    private String path;
    @TableField("embedding_vector") 
    private String embeddingVector;
    @TableField(exist = false)
    private String thumbPath;
}
