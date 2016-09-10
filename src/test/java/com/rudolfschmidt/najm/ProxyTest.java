package com.rudolfschmidt.najm;

import com.rudolfschmidt.najm.exceptions.*;
import com.rudolfschmidt.najm.repositories.*;
import org.junit.Test;

public class ProxyTest {

    @Test(expected = IllegaMongolRepositoryException.class)
    public void noMongoRepository() {
        MongoRepository.newInstance(MissedMongoRepository.class, null);
    }

    @Test(expected = IllegaMongolRepositoryException.class)
    public void illegalRepositoryGeneric() {
        MongoRepository.newInstance(IllegalGenericRepository.class, null);
    }

    @Test(expected = IllegalCollectionAnnotationException.class)
    public void noCollectionModel() {
        MongoRepository.newInstance(NoCollectionModelRepository.class, null);
    }

    @Test(expected = IllegalCollectionAnnotationException.class)
    public void emptyCollectionModel() {
        MongoRepository.newInstance(EmptyCollectionModelRepository.class, null);
    }

    @Test(expected = IllegalIdAnnotationException.class)
    public void noIdModel() {
        MongoRepository.newInstance(NoIdModelRepository.class, null);
    }

    @Test(expected = IllegalIdAnnotationException.class)
    public void invalidIdTypeModel() {
        MongoRepository.newInstance(IllegalIdTypeModelRepository.class, null);
    }

    @Test(expected = IllegalRepositoryMethodNameException.class)
    public void illegalMethodName() {
        MongoRepository.newInstance(IllegalMethodNameRepository.class, null);
    }

    @Test(expected = IllegalRepositoryMethodNameException.class)
    public void illegalMethodNameMixed() {
        MongoRepository.newInstance(IllegalMethodNameMixedRepository.class, null);
    }

    @Test(expected = IllegalRepositoryMethodGenericException.class)
    public void illegalMethodReturnType() {
        MongoRepository.newInstance(IllegalMethodReturnTypeRepository.class, null);
    }

    @Test(expected = IllegalRepositoryMethodGenericException.class)
    public void illegalMethodGeneric() {
        MongoRepository.newInstance(IllegalMethodGenericRepository.class, null);
    }

    @Test(expected = IllegalRepositoryMethodNameException.class)
    public void illegalMethodNameComplex() {
        MongoRepository.newInstance(IllegalMethodNameComplexRepository.class, null);
    }

    @Test(expected = IllegalRepositoryMethodNameException.class)
    public void illegalMethodNameComplexMixed() {
        MongoRepository.newInstance(IllegalMethodNameMixedComplexRepository.class, null);
    }

    @Test(expected = IllegalRepositoryMethodGenericException.class)
    public void illegalMethodReturnTypeComplex() {
        MongoRepository.newInstance(IllegalMethodReturnTypeComplexRepository.class, null);
    }

    @Test(expected = IllegalRepositoryMethodGenericException.class)
    public void illegalMethodGenericComplex() {
        MongoRepository.newInstance(IllegalMethodGenericComplexRepository.class, null);
    }
}
