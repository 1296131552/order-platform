package com.company.user.model.vo;
import java.time.LocalDateTime;

import com.company.common.enums.model.Status;
import com.company.user.enums.model.LoginType;

import lombok.Data;

@Data
public class LoginVo {
    private Long id;
    private String name;
    private String ip;
    private String browser;
    private String operatingSystem;
    private String deviceModel;
    private LoginType type;
    private Status status;
    private String errorMessage;
    private String location;
    private Integer operateTime;
    private LocalDateTime createTime;
}
