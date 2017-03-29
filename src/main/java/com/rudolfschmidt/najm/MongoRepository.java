package com.rudolfschmidt.najm;

import com.mongodb.client.MongoDatabase;
import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.annotations.Id;
import com.rudolfschmidt.najm.exceptions.IllegaMongolRepositoryException;
import com.rudolfschmidt.najm.exceptions.IllegalCollectionNameException;
import com.rudolfschmidt.najm.exceptions.IllegalIdAnnotationException;
import com.rudolfschmidt.najm.exceptions.IllegalMethodSignatureException;

import java.lang.reflect.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MongoRepository {

	public static <T> T newInstance(Class<T> repository, MongoDatabase mongoDatabase) {

		String collectionName = Optional
			.ofNullable(repository)
			.filter(r -> r.isAnnotationPresent(Collection.class))
			.map(r -> r.getAnnotation(Collection.class))
			.map(Collection::value)
			.filter(value -> !value.isEmpty())
			.orElseThrow(IllegalCollectionNameException::new);

		Class<?> modelClazz = Optional
			.ofNullable(repository)
			.filter(Repository.class::isAssignableFrom)
			.map(r -> findModelClazz(r.getGenericInterfaces()))
			.map(Class.class::cast)
			.orElseThrow(IllegaMongolRepositoryException::new);

		Stream.of(modelClazz.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(Id.class))
			.filter(field -> field.getType().equals(String.class))
			.findAny()
			.orElseThrow(IllegalIdAnnotationException::new);

		final Method[] declaredMethods = repository.getDeclaredMethods();
		checkMethodNames(modelClazz, declaredMethods);
		checkMethodReturnTypes(modelClazz, declaredMethods);

		final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		final Class[] interfaces = new Class[]{repository};
		final RepositoryProxy repositoryProxy = new RepositoryProxy<>(modelClazz, collectionName, mongoDatabase);
		final Object proxyInstance = Proxy.newProxyInstance(classLoader, interfaces, repositoryProxy);
		return repository.cast(proxyInstance);

	}

	private static void checkMethodNames(Class<?> modelClazz, Method[] methods) {
		for (Method method : methods) {
			final String name = method.getName();
			if (!name.startsWith(MongoConstants.FIND_BY) && !name.startsWith(MongoConstants.FIND_FIRST_BY)) {
				throw new IllegalMethodSignatureException();
			}
			if (name.startsWith(MongoConstants.FIND_BY)) {
				final String property = extractProperty(name, MongoConstants.FIND_BY);
				if (!matchModelFieldName(modelClazz, property)) {
					throw new IllegalMethodSignatureException();
				}
			}
			if (name.startsWith(MongoConstants.FIND_FIRST_BY)) {
				final String property = extractProperty(name, MongoConstants.FIND_FIRST_BY);
				if (!matchModelFieldName(modelClazz, property)) {
					throw new IllegalMethodSignatureException();
				}
			}
		}
	}

	private static String extractProperty(String methodName, String prefix) {
		final String property = methodName.substring(prefix.length(), methodName.length());
		return Character.toLowerCase(property.charAt(0)) + property.substring(1);
	}

	private static void checkMethodReturnTypes(Class<?> modelClazz, Method[] methods) {
		for (Method method : methods) {
			final Class<?> returnType = method.getReturnType();
			if (!returnType.equals(List.class) && !returnType.equals(modelClazz)) {
				throw new IllegalMethodSignatureException();
			}
			if (returnType.equals(List.class)) {
				final Type genericReturnType = method.getGenericReturnType();
				if (!ParameterizedType.class.isInstance(genericReturnType)) {
					throw new IllegalMethodSignatureException();
				}
				final ParameterizedType parameterizedType = ParameterizedType.class.cast(genericReturnType);
				final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				if (actualTypeArguments.length > 1) {
					throw new IllegalMethodSignatureException();
				}
				final Type actualTypeArgument = actualTypeArguments[0];
				if (!actualTypeArgument.equals(modelClazz)) {
					throw new IllegalMethodSignatureException();
				}
			}
		}
	}

	private static Type findModelClazz(Type... genericInterfaces) {
		for (Type genericInterface : genericInterfaces) {
			if (ParameterizedType.class.isAssignableFrom(genericInterface.getClass())) {
				final ParameterizedType parameterizedType = ParameterizedType.class.cast(genericInterface);
				if (Repository.class.equals(parameterizedType.getRawType())) {
					final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
					if (actualTypeArguments.length > 1) {
						throw new IllegalMethodSignatureException();
					}
					return actualTypeArguments[0];
				}
			}
		}
		for (Type genericType : genericInterfaces) {
			final Class genericClass = Class.class.cast(genericType);
			return findModelClazz(genericClass.getGenericInterfaces());
		}
		throw new IllegaMongolRepositoryException();
	}

	private static boolean matchModelFieldName(Class<?> modelClazz, String property) {
		for (Field field : modelClazz.getDeclaredFields()) {
			if (property.equals(field.getName())) {
				return true;
			}
		}
		for (Field field : modelClazz.getDeclaredFields()) {
			if (property.startsWith(field.getName())) {
				return matchNestedModelMethodName(field, property);
			}
		}
		return false;
	}

	private static boolean matchNestedModelMethodName(Field field, String property) {
		final String name = property.substring(field.getName().length(), property.length());
		final String firstLetterLow = Character.toLowerCase(name.charAt(0)) + name.substring(1);
		return matchModelFieldName(field.getType(), firstLetterLow);
	}
}
