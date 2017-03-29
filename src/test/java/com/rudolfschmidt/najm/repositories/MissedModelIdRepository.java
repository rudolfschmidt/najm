package com.rudolfschmidt.najm.repositories;

import com.rudolfschmidt.najm.Constants;
import com.rudolfschmidt.najm.Repository;
import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.model.NoIdModel;

@Collection(Constants.VALID_MODEL_NAME)
public interface MissedModelIdRepository extends Repository<NoIdModel> {
}
