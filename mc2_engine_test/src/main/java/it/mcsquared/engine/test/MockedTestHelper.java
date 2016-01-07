package it.mcsquared.engine.test;

import it.mcsquared.engine.test.PrivateMethodDetails.PrivateMethodParam;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockedTestHelper {

	public class MockedDaoData {
		protected String fieldName;
		protected Object mockedDao;
	}

	private static Logger log = LoggerFactory.getLogger(MockedTestHelper.class);

	public static void injectMockedInstance(Object parentInstance, String fieldName, Object mockedDao) throws Throwable {
		Field daoField = parentInstance.getClass().getDeclaredField(fieldName);
		daoField.setAccessible(true);
		daoField.set(parentInstance, mockedDao);
	}

	@SuppressWarnings("unchecked")
	public static <T> T invokePrivateMethod(Object instance, PrivateMethodDetails details) throws Throwable {
		List<PrivateMethodParam> params = details.getParams();

		List<Class<?>> paramClasses = new ArrayList<Class<?>>();
		List<Object> paramValues = new ArrayList<Object>();
		for (PrivateMethodParam p : params) {
			paramClasses.add(p.getClazz());
			paramValues.add(p.getValue());
		}

		Method m = details.getClazz().getDeclaredMethod(details.getName(), paramClasses.toArray(new Class<?>[] {}));
		m.setAccessible(true);
		return (T) m.invoke(instance, paramValues.toArray(new Object[] {}));
	}

	public static <T> T readPrivateField(Object instance, String fieldName) throws Exception {
		return readPrivateField(instance.getClass(), instance, fieldName);
	}

	@SuppressWarnings("unchecked")
	public static <T> T readPrivateField(Class<?> clazz, Object instance, String fieldName) throws Exception {
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (T) field.get(instance);
	}
}
