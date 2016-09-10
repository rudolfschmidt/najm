package com.rudolfschmidt.najm.repositories;

import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.model.ComplexModel;

import java.util.List;

public interface IllegalMethodNameComplexRepository extends Repository<ComplexModel> {
    List<ComplexModel> findByNestedModelFoo();
}
