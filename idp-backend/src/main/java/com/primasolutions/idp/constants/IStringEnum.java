package com.primasolutions.idp.constants;

import org.apache.commons.lang.ObjectUtils;

import java.io.Serializable;
import java.util.Arrays;

public interface IStringEnum extends Serializable {
    String getValue();

    String getName();

    boolean equals(String value);


    default <T extends IStringEnum> T create(Class<T> clazz, String value) {
        return create(clazz, value, null);
    }

    default <T extends IStringEnum> T create(Class<T> clazz, String value, T defaultItem) {
        T[] enumItems = clazz.getEnumConstants();
        T item = null;
        if (enumItems != null) {
            item = Arrays.stream(enumItems).filter(enumItem -> ObjectUtils.equals(value, enumItem.getValue()))
                    .findFirst().get();
        }
        return item != null ? item : defaultItem;
    }
}
