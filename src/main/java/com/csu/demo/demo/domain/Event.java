package com.csu.demo.demo.domain;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName(value = "events")
@Data
public class Event {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer user_id;
    private Integer item_id;
    private String action;
    private Float score;
    private LocalDateTime ts;
}
