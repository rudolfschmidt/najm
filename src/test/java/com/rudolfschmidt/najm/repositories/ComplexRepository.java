package com.rudolfschmidt.najm.repositories;

import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.model.ComplexModel;

import java.util.List;

public interface ComplexRepository extends Repository<ComplexModel> {
    List<ComplexModel> findByEmail(String email);
    List<ComplexModel> findByNestedModelToken(String token);
}