package com.rudolfschmidt.najm;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.rudolfschmidt.najm.model.SimpleModel;
import com.rudolfschmidt.najm.repositories.SimpleRepository;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RepositorySimpleTest {

    private static SimpleRepository repository;

    @BeforeClass
    public static void beforeClass() {
        MongoDatabase mongoDatabase = new MongoClient().getDatabase(Constants.DATABASE_NAME);
        repository = MongoRepository.newInstance(SimpleRepository.class, mongoDatabase);
    }

    @After
    public void emptyDatabase() {
        repository.deleteAll();
    }

    @Test
    public void save() {
        SimpleModel expected = SimpleModel.newInstance();
        repository.save(expected);
        SimpleModel actual = repository.findOne(expected.getId()).orElseThrow(RuntimeException::new);
        assertEquals(1, repository.count());
    }

    @Test
    public void update() {
        SimpleModel expected = SimpleModel.newInstance();
        repository.save(expected);
        expected = SimpleModel.newInstance(expected.getId());
        repository.save(expected);
        assertEquals(1, repository.count());
    }

    @Test
    public void findAll() {
        SimpleModel a = SimpleModel.newInstance();
        SimpleModel b = SimpleModel.newInstance();
        repository.save(a);
        repository.save(b);
        repository.findAll()
                .stream()
                .filter(model -> model.getId().equals(a.getId()))
                .filter(model -> model.getEmail().equals(a.getEmail()))
                .filter(model -> model.getPassword().equals(a.getPassword()))
                .findAny().orElseThrow(AssertionError::new);
        repository.findAll()
                .stream()
                .filter(model -> model.getId().equals(b.getId()))
                .filter(model -> model.getEmail().equals(b.getEmail()))
                .filter(model -> model.getPassword().equals(b.getPassword()))
                .findAny().orElseThrow(AssertionError::new);
    }

    @Test
    public void findOne() {
        SimpleModel expected = SimpleModel.newInstance();
        repository.save(expected);
        SimpleModel actual = repository.findOne(expected.getId()).orElseThrow(RuntimeException::new);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getPassword(), actual.getPassword());
    }

    @Test
    public void existsTrue() {
        SimpleModel expected = SimpleModel.newInstance();
        repository.save(expected);
        assertTrue(repository.exists(expected.getId()));
    }

    @Test
    public void existsFalse() {
        assertFalse(repository.exists(ObjectId.get().toString()));
    }

    @Test
    public void count() {
        assertEquals(0, repository.count());
        repository.save(SimpleModel.newInstance());
        assertEquals(1, repository.count());
    }

    @Test
    public void deleteId() {
        SimpleModel a = SimpleModel.newInstance();
        SimpleModel b = SimpleModel.newInstance();
        repository.save(a);
        repository.save(b);
        assertTrue(repository.exists(a.getId()));
        assertTrue(repository.exists(b.getId()));
        repository.delete(a.getId());
        assertFalse(repository.exists(a.getId()));
        assertTrue(repository.exists(b.getId()));
    }

    @Test
    public void deleteEntity() {
        SimpleModel a = SimpleModel.newInstance();
        SimpleModel b = SimpleModel.newInstance();
        repository.save(a);
        repository.save(b);
        assertTrue(repository.exists(a.getId()));
        assertTrue(repository.exists(b.getId()));
        repository.delete(a);
        assertFalse(repository.exists(a.getId()));
        assertTrue(repository.exists(b.getId()));
    }

    @Test
    public void deleteAll() {
        SimpleModel a = SimpleModel.newInstance();
        SimpleModel b = SimpleModel.newInstance();
        repository.save(a);
        repository.save(b);
        assertEquals(2, repository.deleteAll());
        assertFalse(repository.exists(a.getId()));
        assertFalse(repository.exists(b.getId()));
    }
}
