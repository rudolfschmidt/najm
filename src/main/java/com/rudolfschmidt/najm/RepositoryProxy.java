package com.rudolfschmidt.najm;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.rudolfschmidt.najm.annotations.Collection;
import com.rudolfschmidt.najm.annotations.Id;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RepositoryProxy<T> implements InvocationHandler, Repository<T> {

	private final Class<T> modelClazz;
	private final MongoDatabase mongoDatabase;

	public RepositoryProxy(Class<T> modelClazz, MongoDatabase mongoDatabase) {
		this.modelClazz = modelClazz;
		this.mongoDatabase = mongoDatabase;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		Optional<Method> invocation = Stream.of(getClass().getMethods())
				.filter(m -> m.getName().equals(method.getName()))
				.filter(m -> Arrays.equals(m.getParameterTypes(), method.getParameterTypes()))
				.findAny();

		if (invocation.isPresent()) {
			return invocation.get().invoke(this, args);
		} else if (args.length > 0) {
			return Optional.of(method)
					.map(Method::getName)
					.filter(name -> name.startsWith(MongoConstants.FIND_BY))
					.map(name -> name.substring(MongoConstants.FIND_BY.length(), name.length()))
					.map(name -> Character.toLowerCase(name.charAt(0)) + name.substring(1))
					.map(name -> findAll(name, args[0]))
					.orElseGet(Collections::emptyList);
		}
		return null;
	}

	@Override
	public void save(T entity) {
		Document document = serialize(entity);
		Optional<Bson> filter = Stream.of(entity.getClass().getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(Id.class))
				.peek(field -> field.setAccessible(true))
				.findAny()
				.map(field -> getValue(entity, field))
				.map(Object::toString)
				.filter(ObjectId::isValid)
				.map(id -> Filters.eq(MongoConstants.ID, new ObjectId(id)));
		if (filter.isPresent()) {
			collection().replaceOne(filter.get(), document);
		} else {
			collection().insertOne(document);
			Stream.of(entity.getClass().getDeclaredFields())
					.filter(field -> field.isAnnotationPresent(Id.class))
					.peek(field -> field.setAccessible(true))
					.findAny()
					.ifPresent(field -> {
						Object id = document.get(MongoConstants.ID);
						setValue(entity, field, id.toString());
					});
		}
	}

	@Override
	public void saveAll(Iterable<T> entities) {
		entities.forEach(this::save);
	}

	@Override
	public List<T> findAll() {
		return StreamSupport.stream(collection().find().spliterator(), true)
				.map(this::serialize)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<T> findOne(String id) {
		return Optional.ofNullable(id)
				.filter(ObjectId::isValid)
				.map(str -> Filters.eq(MongoConstants.ID, new ObjectId(str)))
				.map(filter -> collection().find(filter).first())
				.map(this::serialize);
	}

	@Override
	public boolean exists(String id) {
		return Optional.ofNullable(id)
				.filter(ObjectId::isValid)
				.map(str -> Filters.eq(MongoConstants.ID, new ObjectId(str)))
				.map(filter -> collection().find(filter).first())
				.isPresent();
	}

	@Override
	public long count() {
		return collection().count();
	}

	@Override
	public long delete(String id) {
		return Optional.ofNullable(id)
				.filter(ObjectId::isValid)
				.map(str -> Filters.eq(MongoConstants.ID, new ObjectId(str)))
				.map(filter -> collection().deleteOne(filter))
				.map(DeleteResult::getDeletedCount)
				.orElse(0L);
	}

	@Override
	public long delete(Object entity) {
		return Optional.ofNullable(entity)
				.map(t -> Stream.of(t.getClass().getDeclaredFields()))
				.orElse(Stream.empty())
				.filter(field -> field.isAnnotationPresent(Id.class))
				.peek(field -> field.setAccessible(true))
				.findAny()
				.map(field -> getValue(entity, field))
				.map(Object::toString)
				.map(this::delete)
				.orElse(0L);
	}

	@Override
	public long deleteAll() {
		return collection().deleteMany(new Document()).getDeletedCount();
	}

	private MongoCollection<Document> collection() {
		return mongoDatabase.getCollection(modelClazz.getAnnotation(Collection.class).value());
	}

	private Object getValue(Object entity, Field field) {
		try {
			return field.get(entity);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void setValue(Object entity, Field field, Object value) {
		try {
			field.set(entity, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Document serialize(Object entity) {
		Document document = new Document();
		Stream.of(entity.getClass().getDeclaredFields())
				.filter(field -> !field.isAnnotationPresent(Id.class))
				.peek(field -> field.setAccessible(true))
				.forEach(field -> Optional.ofNullable(getValue(entity, field)).ifPresent(value -> {
					if (Arrays.asList(MongoConstants.CODEC_TYPES).contains(value.getClass())) {
						document.append(field.getName(), value);
					} else if (Arrays.asList(MongoConstants.STRING_TYPES).contains(value.getClass())) {
						document.append(field.getName(), value.toString());
					} else if (value instanceof Number) {
						document.append(field.getName(), value);
					} else {
						document.append(field.getName(), serialize(value));
					}
				}));
		return document;
	}

	private T serialize(Document document) {
		Object entity = serialize(document, modelClazz);
		Stream.of(entity.getClass().getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(Id.class))
				.peek(field -> field.setAccessible(true))
				.findAny()
				.ifPresent(field -> setValue(entity, field, document.get(MongoConstants.ID).toString()));
		return (T) entity;
	}

	private Object serialize(Document document, Class<?> type) {
		Object entity = Stream.of(type.getDeclaredConstructors())
				.peek(constructor -> constructor.setAccessible(true))
				.findAny()
				.map(this::newInstance)
				.orElseThrow(RuntimeException::new);
		Stream.of(type.getDeclaredFields())
				.filter(field -> !field.isAnnotationPresent(Id.class))
				.peek(field -> field.setAccessible(true))
				.forEach(field -> Optional.ofNullable(document.get(field.getName())).ifPresent(value -> {
					if (value.getClass() == Document.class) {
						setValue(entity, field, serialize((Document) value, field.getType()));
					} else if (value.getClass() == ObjectId.class && field.getType() == String.class) {
						setValue(entity, field, value.toString());
					} else if (field.getType() == LocalTime.class) {
						setValue(entity, field, LocalTime.parse(value.toString()));
					} else {
						setValue(entity, field, value);
					}
				}));
		return entity;
	}

	private Object newInstance(Constructor<?> constructor) {
		try {
			return constructor.newInstance(new Object[constructor.getParameterCount()]);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private List<T> findAll(String property, Object value) {
		String fieldName = getFieldName(modelClazz, property);
		Bson filter = Filters.eq(fieldName, value);
		return StreamSupport.stream(collection().find(filter).spliterator(), false)
				.map(this::serialize)
				.collect(Collectors.toList());
	}

	private String getFieldName(Class<?> type, String property) {
		StringBuilder sb = new StringBuilder();
		Stream.of(type.getDeclaredFields())
				.filter(field -> field.getName().equals(property))
				.findAny()
				.map(Field::getName)
				.ifPresent(sb::append);
		Stream.of(type.getDeclaredFields())
				.filter(field -> !field.getName().equals(property))
				.filter(field -> property.startsWith(field.getName()))
				.peek(field -> sb.append(field.getName()).append("."))
				.findAny()
				.flatMap(field -> getFieldName(field, property))
				.ifPresent(sb::append);
		return sb.toString();
	}

	private Optional<String> getFieldName(Field field, String property) {
		return Optional.of(field)
				.map(Field::getName)
				.map(name -> property.substring(name.length(), property.length()))
				.filter(Characters::isFirstLetterNotLowerCase)
				.map(Characters::makeFirstLetterLowerCase)
				.map(name -> getFieldName(field.getType(), name));
	}

}
