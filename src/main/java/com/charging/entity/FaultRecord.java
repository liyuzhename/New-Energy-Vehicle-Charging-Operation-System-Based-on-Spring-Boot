package com.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fault_record")
public class FaultRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long pileId;

    private String pileNo;

    private String description;

    private String status;

    private String handleNote;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
