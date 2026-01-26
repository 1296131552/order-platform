package com.company.user.model.dto;

import com.company.common.model.dto.BaseListDTO;
import com.company.user.enums.model.UserSex;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetUsersDTO extends BaseListDTO {
    private String username;
    private String name;
    private UserSex sex;
    private List<Integer> roleIds;
    private Boolean isValid;
}
