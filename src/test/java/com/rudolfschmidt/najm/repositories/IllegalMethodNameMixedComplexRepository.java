package com.rudolfschmidt.najm.repositories;

import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.model.ComplexModel;

import java.util.List;

public interface IllegalMethodNameMixedComplexRepository extends Repository<ComplexModel> {
    List<ComplexModel> findByNestedModelToken();
    List<ComplexModel> findByNestedModelFoo();
}
