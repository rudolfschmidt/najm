package com.rudolfschmidt.najm.repositories.findFirstBy;

import com.rudolfschmidt.najm.Constants;
import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.model.ComplexModel;

import java.util.Optional;

@Collection(Constants.VALID_MODEL_NAME)
public interface IllegalMethodReturnTypeComplexRepository extends Repository<ComplexModel> {
	Optional<ComplexModel> findByNestedModelToken(String token);
}
