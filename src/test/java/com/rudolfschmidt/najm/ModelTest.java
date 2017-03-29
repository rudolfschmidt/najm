package com.rudolfschmidt.najm;

import com.rudolfschmidt.najm.exceptions.IllegalIdAnnotationException;
import com.rudolfschmidt.najm.repositories.InvalidModelIdTypeRepository;
import com.rudolfschmidt.najm.repositories.MissedModelIdRepository;
import org.junit.Test;

public class ModelTest {
	@Test(expected = IllegalIdAnnotationException.class)
	public void missedModelIdRepository() {
		MongoRepository.newInstance(MissedModelIdRepository.class, null);
	}
	@Test(expected = IllegalIdAnnotationException.class)
	public void invalidModelIdTypeRepository() {
		MongoRepository.newInstance(InvalidModelIdTypeRepository.class, null);
	}
}
