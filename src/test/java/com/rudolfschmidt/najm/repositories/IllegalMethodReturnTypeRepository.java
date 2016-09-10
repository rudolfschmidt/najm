package com.rudolfschmidt.najm.repositories;

import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.model.ComplexModel;

import java.util.Optional;

public interface IllegalMethodReturnTypeRepository extends Repository<ComplexModel> {
    Optional<ComplexModel> findByEmail(String email);
}
