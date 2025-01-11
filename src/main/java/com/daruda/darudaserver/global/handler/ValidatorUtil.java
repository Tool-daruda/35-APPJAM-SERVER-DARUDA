package com.daruda.darudaserver.global.handler;


import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatorUtil {

    public static <T> void validateListSizeMin(final List<T> list, int minSize, ErrorCode errorCode){
        if(list.isEmpty() || list.size() < minSize){
            throw new InvalidValueException(errorCode);
        }
    }

    public static <T> void validateListSizeMax(final List<T> list, int maxSize, ErrorCode errorCode){
        if(list.isEmpty() || list.size() > maxSize){
            throw new InvalidValueException(errorCode);
        }
    }

    public static void validStringMinSize(final String string, int minSize, ErrorCode code) {
        if (string.length() < minSize) {
            throw new InvalidValueException(code);
        }
    }
}
