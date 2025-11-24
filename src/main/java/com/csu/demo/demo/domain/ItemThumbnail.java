package com.csu.demo.demo.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("item_thumbnails")
@Data
public class ItemThumbnail {
    @TableId
    private Integer itemId;
    private String thumbPath;
}
