package com.rudolfschmidt.najm.model;

import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.annotations.Id;

import java.util.UUID;

@Collection("model")
public class SimpleModel {

    public static SimpleModel newInstance() {
        return new SimpleModel(null, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    public static SimpleModel newInstance(String id) {
        return new SimpleModel(id, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }


    @Id
    private final String id;
    private final String email;
    private final String password;

    private SimpleModel(String id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
