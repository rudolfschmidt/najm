package com.rudolfschmidt.najm;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.rudolfschmidt.najm.annotations.Id;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class RepositoryProxy<T> implements InvocationHandler, Repository<T> {

	private final Class<T> modelClazz;
	private final String collectionName;
	private final MongoDatabase mongoDatabase;

	RepositoryProxy(Class<T> modelClazz, String collectionName, MongoDatabase mongoDatabase) {
		this.modelClazz = modelClazz;
		this.collectionName = collectionName;
		this.mongoDatabase = mongoDatabase;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		final Method invocation = Stream.of(getClass().getMethods())
			.filter(m -> m.getName().equals(method.getName()))
			.filter(m -> Arrays.equals(m.getParameterTypes(), method.getParameterTypes()))
			.findAny()
			.orElse(null);

		if (invocation != null) {
			return invocation.invoke(this, args);
		} else {
			final String name = method.getName();
			if (name.startsWith(MongoConstants.FIND_BY)) {
				final String property = name.substring(MongoConstants.FIND_BY.length(), name.length());
				final String lowerCase = Character.toLowerCase(property.charAt(0)) + property.substring(1);
				return findAll(lowerCase, args[0]);
			} else if (name.startsWith(MongoConstants.FIND_FIRST_BY)) {
				final String property = name.substring(MongoConstants.FIND_FIRST_BY.length(), name.length());
				final String lowerCase = Character.toLowerCase(property.charAt(0)) + property.substring(1);
				return findFirst(lowerCase, args[0]);
			}
		}
		return null;
	}

	@Override
	public void save(T entity) {
		if (entity == null) {
			return;
		}
		final String identifier = Stream.of(entity.getClass().getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Id.class))
			.peek(f -> f.setAccessible(true))
			.findAny()
			.map(f -> {
				try {
					return f.get(entity);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			})
			.map(String::valueOf)
			.filter(ObjectId::isValid)
			.orElse(null);

		final Document document = serialize(entity);
		if (identifier != null) {
			Bson filter = Filters.eq(MongoConstants.ID, new ObjectId(identifier));
			collection().replaceOne(filter, document);
		} else {
			collection().insertOne(document);
			Stream.of(entity.getClass().getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(Id.class))
				.peek(f -> f.setAccessible(true))
				.findAny()
				.ifPresent(f -> {
					try {
						Object value = document.get(MongoConstants.ID);
						String str = String.valueOf(value);
						f.set(entity, str);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				});
		}
	}

	@Override
	public void saveAll(Iterable<T> entities) {
		if (entities == null) {
			return;
		}
		for (T entity : entities) {
			save(entity);
		}
	}

	@Override
	public List<T> findAll() {
		final List<T> result = new ArrayList<T>();
		for (Document document : collection().find()) {
			final T entity;
			try {
				entity = toEntity(document);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
			result.add(entity);
		}
		return result;
	}

	@Override
	public T findOne(String id) {
		if (id == null) {
			return null;
		}
		if (!ObjectId.isValid(id)) {
			return null;
		}
		final Bson filter = Filters.eq(MongoConstants.ID, new ObjectId(id));
		final Document document = collection().find(filter).first();
		try {
			return toEntity(document);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean exists(String id) {
		if (id == null) {
			return false;
		}
		if (!ObjectId.isValid(id)) {
			return false;
		}
		final Bson filter = Filters.eq(MongoConstants.ID, new ObjectId(id));
		return collection().count(filter) > 0;
	}

	@Override
	public long count() {
		return collection().count();
	}

	@Override
	public long delete(String id) {
		if (id == null) {
			return 0;
		}
		if (!ObjectId.isValid(id)) {
			return 0;
		}
		final Bson filter = Filters.eq(MongoConstants.ID, new ObjectId(id));
		return collection().deleteOne(filter).getDeletedCount();
	}

	@Override
	public long delete(Object entity) {
		if (entity == null) {
			return 0;
		}
		for (Field field : entity.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class)) {
				field.setAccessible(true);
				final Object value;
				try {
					value = field.get(entity);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException();
				}
				if (value.getClass() != String.class) {
					throw new RuntimeException();
				}
				final String id = String.valueOf(value);
				return delete(id);
			}
		}
		return 0;
	}

	@Override
	public long deleteAll() {
		return collection().deleteMany(new Document()).getDeletedCount();
	}

	private MongoCollection<Document> collection() {
		return mongoDatabase.getCollection(collectionName);
	}

	private Document serialize(Object entity) {
		final Document document = new Document();
		for (Field field : entity.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			final Object fieldValue;
			try {
				fieldValue = field.get(entity);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			if (fieldValue == null) {
				continue;
			}
			if (Arrays.asList(MongoConstants.CODEC_TYPES).contains(fieldValue.getClass())) {
				document.append(field.getName(), fieldValue);
			} else if (Arrays.asList(MongoConstants.STRING_TYPES).contains(fieldValue.getClass())) {
				document.append(field.getName(), fieldValue.toString());
			} else if (fieldValue instanceof Number) {
				document.append(field.getName(), fieldValue);
			} else {
				document.append(field.getName(), serialize(fieldValue));
			}
		}
		return document;
	}

	private T toEntity(Document document) throws ReflectiveOperationException {
		if (document == null) {
			return null;
		}
		final Object entity = serialize(document, modelClazz);
		for (Field field : entity.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class)) {
				field.setAccessible(true);
				field.set(entity, document.get(MongoConstants.ID).toString());
			}
		}
		return modelClazz.cast(entity);
	}

	private Object serialize(Document document, Class<?> type) throws ReflectiveOperationException {
		final Object entity = newInstance(type.getDeclaredConstructors());
		for (Field field : type.getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class)) {
				continue;
			}
			field.setAccessible(true);
			final String fieldName = field.getName();
			final Object value = document.get(fieldName);
			if (value == null) {
				continue;
			}
			if (value.getClass() == Document.class) {
				field.set(entity, serialize(Document.class.cast(value), field.getType()));
			} else if (value.getClass() == ObjectId.class && field.getType() == String.class) {
				field.set(entity, value.toString());
			} else if (field.getType() == LocalTime.class) {
				field.set(entity, LocalTime.parse(value.toString()));
			} else {
				field.set(entity, value);
			}
		}
		return entity;
	}

	private Object newInstance(Constructor<?>[] declaredConstructors) throws ReflectiveOperationException {
		for (Constructor<?> constructor : declaredConstructors) {
			constructor.setAccessible(true);
			return constructor.newInstance(new Object[constructor.getParameterCount()]);
		}
		return null;
	}

	private T findFirst(String property, Object value) throws ReflectiveOperationException {
		final String fieldName = getFieldName(modelClazz, property);
		final Bson filter = Filters.eq(fieldName, value);
		final Document document = collection().find(filter).first();
		return toEntity(document);
	}

	private List<T> findAll(String property, Object value) throws ReflectiveOperationException {
		final String fieldName = getFieldName(modelClazz, property);
		final Bson filter = Filters.eq(fieldName, value);

		final List<T> ret = new ArrayList<>();
		for (Document document : collection().find(filter)) {
			ret.add(toEntity(document));
		}
		return ret;
	}

	private String getFieldName(Class<?> type, String property) {
		final StringBuilder sb = new StringBuilder();

		for (Field field : type.getDeclaredFields()) {
			if (field.getName().equals(property)) {
				sb.append(field.getName());
			} else if (property.startsWith(field.getName())) {
				sb.append(field.getName()).append(".").append(getFieldName(field, property));
			}
		}
		return sb.toString();
	}

	private String getFieldName(Field field, String property) {
		final String name = property.substring(field.getName().length(), property.length());
		final String lowerCase = Character.toLowerCase(name.charAt(0)) + name.substring(1);
		return getFieldName(field.getType(), lowerCase);
	}
}
