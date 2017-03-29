package com.rudolfschmidt.najm;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.rudolfschmidt.najm.model.ComplexModel;
import com.rudolfschmidt.najm.repositories.findBy.ComplexModelRepository;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersistenceWithFindByMethodsTest {

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
	public void findByEmail() {
		ComplexModel expected = ComplexModel.newInstance();
		repository.save(expected);
		ComplexModel actual = repository
			.findByEmail(expected.getEmail())
			.stream()
			.findAny()
			.orElseThrow(IllegalArgumentException::new);
		assertEquals(expected.getId(), actual.getId());
	}

	@Test
	public void findByNestedModelToken() {
		ComplexModel expected = ComplexModel.newInstance();
		repository.save(expected);
		ComplexModel actual = repository
			.findByNestedModelToken(expected.getNestedModel().getToken())
			.stream()
			.findAny()
			.orElseThrow(IllegalArgumentException::new);
		assertEquals(expected.getNestedModel().getToken(), actual.getNestedModel().getToken());
	}
}
