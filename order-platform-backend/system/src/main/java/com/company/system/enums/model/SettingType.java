package com.company.system.enums.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@AllArgsConstructor
@Getter
public enum SettingType {
    BOOLEAN(Boolean.class),
    INTEGER(Integer.class),
    STRING(String.class),
    INTEGER_LIST(List.class, Integer.class),
    ;

    private final Class<?> mainType;
    private final Class<?> elementType;

    SettingType(Class<?> mainType) {
        this.mainType = mainType;
        this.elementType = null;
    }

    public boolean isList() {
        return List.class.equals(mainType);
    }
}
