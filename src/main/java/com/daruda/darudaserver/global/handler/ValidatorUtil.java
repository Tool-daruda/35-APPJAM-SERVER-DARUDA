package com.daruda.darudaserver.global.handler;


import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatorUtil {

    private static int MIN_NUm =0 ;

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

    public static void validatePage(final int page) {
            if (page <= MIN_NUm) {
                throw new InvalidValueException(ErrorCode.INVALID_PAGE_MIN_SIZE);
            }
    }

    public static void validateSize(final int size, final int maxSize) {
            if (size <= MIN_NUm || size > maxSize) {
                throw new InvalidValueException(ErrorCode.INVALID_PAGE_MAX_SIZE);
            }
        }
    }

