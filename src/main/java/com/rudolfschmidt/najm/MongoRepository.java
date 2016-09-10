package com.rudolfschmidt.najm;

import com.mongodb.client.MongoDatabase;
import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.annotations.Id;
import com.rudolfschmidt.najm.exceptions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MongoRepository {

	public static <T> T newInstance(Class<T> repository, MongoDatabase mongoDatabase) {

		Optional.ofNullable(repository)
				.filter(Repository.class::isAssignableFrom)
				.orElseThrow(IllegaMongolRepositoryException::new);
		Class<?> modelClazz = Stream.of(repository.getGenericInterfaces())
				.findFirst()
				.filter(type -> type instanceof ParameterizedType)
				.map(ParameterizedType.class::cast)
				.map(ParameterizedType::getActualTypeArguments)
				.map(Stream::of)
				.orElse(Stream.empty())
				.findFirst()
				.map(Class.class::cast)
				.orElseThrow(IllegaMongolRepositoryException::new);

		Optional.of(modelClazz)
				.filter(clazz -> clazz.isAnnotationPresent(Collection.class))
				.filter(clazz -> !clazz.getAnnotation(Collection.class).value().isEmpty())
				.orElseThrow(IllegalCollectionAnnotationException::new);
		Stream.of(modelClazz.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(Id.class))
				.filter(field -> field.getType() == String.class)
				.findAny()
				.orElseThrow(IllegalIdAnnotationException::new);

		Optional.of(repository.getDeclaredMethods())
				.filter(methods -> methods.length > 0)
				.ifPresent(declaredMethods -> checkRepositoryMethods(modelClazz, declaredMethods));

		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		Class[] interfaces = new Class[]{repository};
		RepositoryProxy repositoryProxy = new RepositoryProxy<>(modelClazz, mongoDatabase);
		Object proxyInstance = Proxy.newProxyInstance(classLoader, interfaces, repositoryProxy);
		return (T) proxyInstance;
	}

	private static void checkRepositoryMethods(Class<?> modelClazz, Method[] declaredMethods) {
		Optional.of(declaredMethods)
				.filter(methods -> checkMethodNames(modelClazz, methods))
				.orElseThrow(IllegalRepositoryMethodNameException::new);
		Optional.of(declaredMethods)
				.filter(methods -> checkReturnTypes(modelClazz, methods))
				.orElseThrow(IllegalRepositoryMethodGenericException::new);
	}

	private static boolean checkMethodNames(Class<?> modelClazz, Method[] declaredMethods) {
		return Stream.of(declaredMethods)
				.map(Method::getName)
				.filter(name -> name.startsWith(MongoConstants.FIND_BY))
				.map(name -> name.substring(MongoConstants.FIND_BY.length(), name.length()))
				.filter(Characters::isFirstLetterNotLowerCase)
				.map(Characters::makeFirstLetterLowerCase)
				.allMatch(name -> matchModelMethodName(modelClazz, name));
	}

	private static boolean matchModelMethodName(Class<?> modelClazz, String name) {
		if (Stream.of(modelClazz.getDeclaredFields()).anyMatch(field -> field.getName().equals(name))) {
			return true;
		}
		return Stream.of(modelClazz.getDeclaredFields())
				.filter(field -> name.startsWith(field.getName()))
				.anyMatch(field -> matchNestedModelMethodName(field, name));
	}

	private static boolean matchNestedModelMethodName(Field field, String name) {
		return Optional.of(field.getName())
				.map(fieldName -> name.substring(fieldName.length(), name.length()))
				.filter(Characters::isFirstLetterNotLowerCase)
				.map(Characters::makeFirstLetterLowerCase)
				.map(fieldName -> matchModelMethodName(field.getType(), fieldName))
				.orElse(false);
	}

	private static boolean checkReturnTypes(Class<?> modelClazz, Method[] declaredMethods) {
		return Stream.of(declaredMethods)
				.filter(method -> method.getReturnType() == List.class)
				.map(Method::getGenericReturnType)
				.filter(type -> type instanceof ParameterizedType)
				.map(ParameterizedType.class::cast)
				.map(ParameterizedType::getActualTypeArguments)
				.anyMatch(types -> Stream.of(types).allMatch(modelClazz::equals));
	}
}
