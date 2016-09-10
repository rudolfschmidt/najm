package com.rudolfschmidt.najm.model;

import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.annotations.Id;

import java.util.UUID;

@Collection("model")
public class ComplexModel {

    public static ComplexModel newInstance() {
        NestedModel nestedModel = NestedModel.newInstance();
        return new ComplexModel(null, UUID.randomUUID().toString(), UUID.randomUUID().toString(), nestedModel);
    }

    public static ComplexModel newInstance(String id) {
        NestedModel nestedModel = NestedModel.newInstance();
        return new ComplexModel(id, UUID.randomUUID().toString(), UUID.randomUUID().toString(), nestedModel);
    }

    @Id
    private final String id;
    private final String email;
    private final String password;
    private final NestedModel nestedModel;

    private ComplexModel(String id, String email, String password, NestedModel nestedModel) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nestedModel = nestedModel;
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

    public NestedModel getNestedModel() {
        return nestedModel;
    }


}
