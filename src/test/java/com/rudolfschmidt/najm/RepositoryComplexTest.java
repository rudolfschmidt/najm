package com.rudolfschmidt.najm;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.rudolfschmidt.najm.model.ComplexModel;
import com.rudolfschmidt.najm.repositories.ComplexRepository;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RepositoryComplexTest {

    private static ComplexRepository repository;

    @BeforeClass
    public static void beforeClass() {
        MongoDatabase mongoDatabase = new MongoClient().getDatabase(Constants.DATABASE_NAME);
        repository = MongoRepository.newInstance(ComplexRepository.class, mongoDatabase);
    }

    @After
    public void emptyDatabase() {
        repository.deleteAll();
    }

    @Test
    public void save() {
        ComplexModel expected = ComplexModel.newInstance();
        repository.save(expected);
        ComplexModel actual = repository.findOne(expected.getId()).orElseThrow(AssertionError::new);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getPassword(), actual.getPassword());
        assertEquals(expected.getNestedModel().getToken(), actual.getNestedModel().getToken());
        assertEquals(expected.getNestedModel().getExpire(), actual.getNestedModel().getExpire());
    }

    @Test
    public void update() {
        ComplexModel expected = ComplexModel.newInstance();
        repository.save(expected);
        expected = ComplexModel.newInstance(expected.getId());
        repository.save(expected);
        assertEquals(1, repository.count());
    }
}
