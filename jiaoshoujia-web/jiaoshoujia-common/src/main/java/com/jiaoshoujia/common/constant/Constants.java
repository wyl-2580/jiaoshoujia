package com.jiaoshoujia.common.constant;

public final class Constants {

    private Constants() {
    }

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String TOKEN_HEADER = "Authorization";

    public static final String LOGIN_USER_KEY = "login_user_key";

    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    public static final String LOGIN_ATTEMPT_KEY = "login_attempt:";

    public static final int MAX_LOGIN_ATTEMPTS = 5;

    public static final int LOCK_DURATION_MINUTES = 30;

    public static final String UTF8 = "UTF-8";

    public static final String GBK = "GBK";

    public static final String RESOURCE_PREFIX = "/profile";
}
