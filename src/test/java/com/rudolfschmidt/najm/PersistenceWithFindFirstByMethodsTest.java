package com.rudolfschmidt.najm;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.rudolfschmidt.najm.model.ComplexModel;
import com.rudolfschmidt.najm.repositories.findFirstBy.ComplexModelRepository;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersistenceWithFindFirstByMethodsTest {

	private final static ComplexModelRepository repository;

	static {
		MongoDatabase mongoDatabase = new MongoClient().getDatabase(Constants.DATABASE_NAME);
		repository = MongoRepository.newInstance(ComplexModelRepository.class, mongoDatabase);
	}

	@After
	public void emptyDatabase() {
		repository.deleteAll();
	}

	@Test
	public void findFirstByEmail() {
		ComplexModel expected = ComplexModel.newInstance();
		repository.save(expected);
		assertEquals(expected.getId(), repository.findFirstByEmail(expected.getEmail()).getId());
	}

	@Test
	public void findFirstByNestedModelToken() {
		ComplexModel expected = ComplexModel.newInstance();
		repository.save(expected);
		ComplexModel actual = repository.findFirstByNestedModelToken(expected.getNestedModel().getToken());
		assertEquals(expected.getNestedModel().getToken(), actual.getNestedModel().getToken());
	}
}
