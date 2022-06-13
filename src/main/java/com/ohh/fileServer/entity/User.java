package com.ohh.fileServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "user")
public class User implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private String account;
    private String password;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
