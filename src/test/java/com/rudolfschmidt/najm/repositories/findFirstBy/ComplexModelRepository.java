package com.rudolfschmidt.najm.repositories.findFirstBy;

import com.rudolfschmidt.najm.Constants;
import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.model.ComplexModel;

@Collection(Constants.VALID_MODEL_NAME)
public interface ComplexModelRepository extends Repository<ComplexModel> {
	ComplexModel findFirstByEmail(String email);
	ComplexModel findFirstByNestedModelToken(String token);
}