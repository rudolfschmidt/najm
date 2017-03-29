package com.rudolfschmidt.najm.repositories.findFirstBy;

import com.rudolfschmidt.najm.Constants;
import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.model.SimpleModel;

import java.util.List;

@Collection(Constants.VALID_MODEL_NAME)
public interface IllegalMethodNameMixedRepository extends Repository<SimpleModel> {
	List<SimpleModel> findByInvalid(String email);
	List<SimpleModel> findByEmail(String email);
}
