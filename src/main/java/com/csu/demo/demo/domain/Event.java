package com.csu.demo.demo.domain;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName(value="events")
@Data
public class Event {
    @TableId(value="id")
    private int id;
    private int user_id;
    private int item_id;
    private String action;
    private float score;
    private LocalDateTime ts;
}
