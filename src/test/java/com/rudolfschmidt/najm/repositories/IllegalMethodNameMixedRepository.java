package com.rudolfschmidt.najm.repositories;

import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.model.SimpleModel;

import java.util.List;

public interface IllegalMethodNameMixedRepository extends Repository<SimpleModel> {
    List<SimpleModel> findByInvalid(String email);
    List<SimpleModel> findByEmail(String email);
}
