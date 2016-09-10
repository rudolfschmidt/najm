package com.rudolfschmidt.najm.model;

import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.annotations.Id;

@Collection("model")
public class InvalidIdTypeModel {
    @Id
    private Object id;
}
