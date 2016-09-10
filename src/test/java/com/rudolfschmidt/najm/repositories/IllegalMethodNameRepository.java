package com.rudolfschmidt.najm.repositories;

import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.model.ComplexModel;
import com.rudolfschmidt.najm.model.SimpleModel;

import java.util.List;

public interface IllegalMethodNameRepository extends Repository<ComplexModel> {
    List<SimpleModel> findByInvalid(String email);
}
