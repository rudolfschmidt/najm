package com.rudolfschmidt.najm;

import com.rudolfschmidt.najm.exceptions.IllegaMongolRepositoryException;
import com.rudolfschmidt.najm.exceptions.IllegalCollectionNameException;
import com.rudolfschmidt.najm.repositories.*;
import org.junit.Test;

public class RepositoryClassSignatureTest {

	@Test(expected = IllegalCollectionNameException.class)
	public void noCollectionName() {
		MongoRepository.newInstance(NoCollectionNameRepository.class, null);
	}

	@Test(expected = IllegalCollectionNameException.class)
	public void emptyCollectionName() {
		MongoRepository.newInstance(EmptyCollectionNameRepository.class, null);
	}

	@Test(expected = IllegaMongolRepositoryException.class)
	public void missedMongoRepository() {
		MongoRepository.newInstance(MissedRepositoryInterface.class, null);
	}

	@Test(expected = IllegaMongolRepositoryException.class)
	public void missedMongoRepositoryGenericRepository() {
		MongoRepository.newInstance(MissedRepositoryGenericRepository.class, null);
	}

	@Test(expected = IllegaMongolRepositoryException.class)
	public void missedMongoRepositoryExtending() {
		MongoRepository.newInstance(ExtendingRepository.class, null);
	}

	@Test(expected = IllegaMongolRepositoryException.class)
	public void missedMongoRepositoryMoreExtending() {
		MongoRepository.newInstance(MoreExtendingRepository.class, null);
	}
}
