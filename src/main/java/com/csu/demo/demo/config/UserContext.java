package com.csu.demo.demo.config;

public class UserContext {
    private static final ThreadLocal<Integer> USER_ID_HOLDER = new ThreadLocal<>();

    public static void setUserId(Integer userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Integer getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
