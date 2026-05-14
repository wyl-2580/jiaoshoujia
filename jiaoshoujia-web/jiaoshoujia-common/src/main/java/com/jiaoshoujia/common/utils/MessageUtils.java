package com.jiaoshoujia.common.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public final class MessageUtils {

    private static MessageSource messageSource;

    private MessageUtils() {
    }

    public static void setMessageSource(MessageSource messageSource) {
        MessageUtils.messageSource = messageSource;
    }

    public static String message(String code, Object... args) {
        if (messageSource == null) {
            return code;
        }
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }
}
