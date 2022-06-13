package com.ohh.fileServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName(value = "user")
public class User implements Serializable {
    @TableId(type = IdType.ID_WORKER)
    private Long id;
    private String name;
    private String account;
    private String password;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT)
    private Date updateTime;
    @TableLogic
    private Integer isDeleted;
    private String salt;
}
