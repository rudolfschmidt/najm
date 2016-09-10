package com.rudolfschmidt.najm.repositories;

import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.model.ComplexModel;

import java.util.Optional;

public interface IllegalMethodReturnTypeComplexRepository extends Repository<ComplexModel> {
    Optional<ComplexModel> findByNestedModelToken(String token);
}
