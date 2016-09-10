package com.rudolfschmidt.najm.model;

import java.util.Date;
import java.util.UUID;

public class NestedModel {

    public static NestedModel newInstance() {
        return new NestedModel(UUID.randomUUID().toString(), new Date());
    }

    private final String token;
    private final Date expire;

    private NestedModel(String token, Date expire) {
        this.token = token;
        this.expire = expire;
    }

    public String getToken() {
        return token;
    }

    public Date getExpire() {
        return expire;
    }
}
