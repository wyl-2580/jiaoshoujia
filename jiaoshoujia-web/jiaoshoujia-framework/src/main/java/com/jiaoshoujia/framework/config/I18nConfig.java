package com.jiaoshoujia.framework.config;

import com.jiaoshoujia.common.utils.MessageUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class I18nConfig {

    private final MessageSource messageSource;

    public I18nConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @PostConstruct
    public void init() {
        MessageUtils.setMessageSource(messageSource);
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.CHINA);
        resolver.setSupportedLocales(List.of(Locale.CHINA, Locale.US, Locale.ENGLISH));
        return resolver;
    }
}
