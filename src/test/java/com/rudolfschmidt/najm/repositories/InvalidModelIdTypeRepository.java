package com.rudolfschmidt.najm.repositories;

import com.rudolfschmidt.najm.Constants;
import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.model.InvalidIdTypeModel;

@Collection(Constants.VALID_MODEL_NAME)
public interface InvalidModelIdTypeRepository extends Repository<InvalidIdTypeModel> {
}
