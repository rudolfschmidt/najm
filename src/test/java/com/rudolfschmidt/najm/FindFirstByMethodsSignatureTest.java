package com.rudolfschmidt.najm;

import com.rudolfschmidt.najm.exceptions.IllegalMethodSignatureException;
import com.rudolfschmidt.najm.repositories.findFirstBy.*;
import org.junit.Test;

public class FindFirstByMethodsSignatureTest {

	@Test(expected = IllegalMethodSignatureException.class)
	public void illegalMethodName() {
		MongoRepository.newInstance(IllegalMethodNameRepository.class, null);
	}

	@Test(expected = IllegalMethodSignatureException.class)
	public void illegalMethodNameMixed() {
		MongoRepository.newInstance(IllegalMethodNameMixedRepository.class, null);
	}

	@Test(expected = IllegalMethodSignatureException.class)
	public void illegalMethodReturnType() {
		MongoRepository.newInstance(IllegalMethodReturnTypeRepository.class, null);
	}

	@Test(expected = IllegalMethodSignatureException.class)
	public void illegalMethodGeneric() {
		MongoRepository.newInstance(IllegalMethodGenericRepository.class, null);
	}

	@Test(expected = IllegalMethodSignatureException.class)
	public void illegalMethodNameComplex() {
		MongoRepository.newInstance(IllegalMethodNameComplexRepository.class, null);
	}

	@Test(expected = IllegalMethodSignatureException.class)
	public void illegalMethodNameComplexMixed() {
		MongoRepository.newInstance(IllegalMethodNameMixedComplexRepository.class, null);
	}

	@Test(expected = IllegalMethodSignatureException.class)
	public void illegalMethodReturnTypeComplex() {
		MongoRepository.newInstance(IllegalMethodReturnTypeComplexRepository.class, null);
	}

	@Test(expected = IllegalMethodSignatureException.class)
	public void illegalMethodGenericComplex() {
		MongoRepository.newInstance(IllegalMethodGenericComplexRepository.class, null);
	}
}
