package com.github.ljtfreitas.restify.http.contract;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.Form.Field;

public class FormObjectsTest {

	private FormObjects formObjects;

	@Before
	public void setup() {
		formObjects = FormObjects.cache();
	}

	@Test
	public void shouldSerializeFormObjectUsingDeclaredFieldNames() {
		MySimpleFormObject mySimpleFormObject = new MySimpleFormObject();
		mySimpleFormObject.name = "Tiago de Freitas Lima";
		mySimpleFormObject.age = 31;

		FormObject formObject = formObjects.of(MySimpleFormObject.class);

		String expected= "name=Tiago+de+Freitas+Lima&age=31";

		assertEquals(expected, formObject.serialize(mySimpleFormObject));
	}

	@Test
	public void shouldSerializeFormObjectOnlyWithNonNullFields() {
		MySimpleFormObject mySimpleFormObject = new MySimpleFormObject();
		mySimpleFormObject.age = 31;

		FormObject formObject = formObjects.of(MySimpleFormObject.class);

		String expected= "age=31";

		assertEquals(expected, formObject.serialize(mySimpleFormObject));
	}

	@Test
	public void shouldSerializeFormObjectWithIterableField() {
		MyIterableFormObject myIterableFormObject = new MyIterableFormObject();
		myIterableFormObject.names = Arrays.asList("Tiago", "Freitas", "Lima");

		FormObject formObject = formObjects.of(MyIterableFormObject.class);

		String expected= "name=Tiago&name=Freitas&name=Lima";

		assertEquals(expected, formObject.serialize(myIterableFormObject));
	}

	@Test
	public void shouldSerializeFormObjectWithIndexedIterableField() {
		MyIndexedIterableFormObject myIndexedIterableFormObject = new MyIndexedIterableFormObject();
		myIndexedIterableFormObject.names = Arrays.asList("Tiago", "Freitas", "Lima");

		FormObject formObject = formObjects.of(MyIndexedIterableFormObject.class);

		String expected= "name[0]=Tiago&name[1]=Freitas&name[2]=Lima";

		assertEquals(expected, formObject.serialize(myIndexedIterableFormObject));
	}

	@Test
	public void shouldSerializeFormObjectWithIndexedIterableOfFormObjectsField() {
		MyComplexIndexedIterableFormObject myIndexedIterableFormObject = new MyComplexIndexedIterableFormObject();

		MySimpleFormObject mySimpleFormObject1 = new MySimpleFormObject();
		mySimpleFormObject1.name = "Tiago de Freitas Lima";
		mySimpleFormObject1.age = 31;

		MySimpleFormObject mySimpleFormObject2 = new MySimpleFormObject();
		mySimpleFormObject2.name = "William Shakespeare";
		mySimpleFormObject2.age = 25;

		myIndexedIterableFormObject.objects = Arrays.asList(mySimpleFormObject1, mySimpleFormObject2);

		FormObject formObject = formObjects.of(MyComplexIndexedIterableFormObject.class);

		String expected= "object[0].name=Tiago+de+Freitas+Lima&object[0].age=31&"
				+ "object[1].name=William+Shakespeare&object[1].age=25";

		assertEquals(expected, formObject.serialize(myIndexedIterableFormObject));
	}

	@Test
	public void shouldSerializeFormObjectUsingCustomizedFieldNames() {
		MyCustomizedFormObject myCustomizedFormObject = new MyCustomizedFormObject();
		myCustomizedFormObject.name = "Tiago de Freitas Lima";
		myCustomizedFormObject.age = 31;

		FormObject formObject = formObjects.of(MyCustomizedFormObject.class);

		String expected= "whatever_name=Tiago+de+Freitas+Lima&whatever_age=31";

		assertEquals(expected, formObject.serialize(myCustomizedFormObject));
	}

	@Test
	public void shouldSerializeFormObjectUsingPrefix() {
		MyPrefixedFormObject myPrefixedFormObject = new MyPrefixedFormObject();
		myPrefixedFormObject.name = "Tiago de Freitas Lima";
		myPrefixedFormObject.age = 31;

		FormObject formObject = formObjects.of(MyPrefixedFormObject.class);

		String expected= "object.name=Tiago+de+Freitas+Lima&object.age=31";

		assertEquals(expected, formObject.serialize(myPrefixedFormObject));
	}

	@Test
	public void shouldSerializeFormObjectWithNestedField() {
		MyNestedFormObject myNestedFormObject = new MyNestedFormObject();
		myNestedFormObject.name = "Tiago de Freitas Lima";

		MySimpleFormObject mySimpleFormObject = new MySimpleFormObject();
		mySimpleFormObject.name = "Tiago de Freitas Lima";
		mySimpleFormObject.age = 31;

		myNestedFormObject.nested = mySimpleFormObject;

		FormObject formObject = formObjects.of(MyNestedFormObject.class);

		String expected= "name=Tiago+de+Freitas+Lima&nested.name=Tiago+de+Freitas+Lima&nested.age=31";

		assertEquals(expected, formObject.serialize(myNestedFormObject));
	}

	@Test
	public void shouldSerializeFormObjectWithNestedFieldUsingCustomizedName() {
		MyCustomizedNestedFormObject myCustomizedNestedFormObject = new MyCustomizedNestedFormObject();

		MySimpleFormObject mySimpleFormObject = new MySimpleFormObject();
		mySimpleFormObject.name = "Tiago de Freitas Lima";
		mySimpleFormObject.age = 31;

		myCustomizedNestedFormObject.nested = mySimpleFormObject;

		FormObject formObject = formObjects.of(MyCustomizedNestedFormObject.class);

		String expected= "whatever.name=Tiago+de+Freitas+Lima&whatever.age=31";

		assertEquals(expected, formObject.serialize(myCustomizedNestedFormObject));
	}

	@Test
	public void shouldSerializeComplexFormObjectWithMultiplesFields() {
		MyComplexFormObject myComplexFormObject = new MyComplexFormObject();
		myComplexFormObject.name = "Tiago de Freitas Lima";
		myComplexFormObject.age = 31;

		MyIterableFormObject myIterableFormObject = new MyIterableFormObject();
		myIterableFormObject.names = Arrays.asList("Tiago", "Freitas", "Lima");
		myComplexFormObject.iterable = myIterableFormObject;

		MyPrefixedFormObject myPrefixedFormObject = new MyPrefixedFormObject();
		myPrefixedFormObject.name = "Tiago de Freitas Lima";
		myPrefixedFormObject.age = 31;
		myComplexFormObject.prefixed = myPrefixedFormObject;

		MySimpleFormObject mySimpleFormObject = new MySimpleFormObject();
		mySimpleFormObject.name = "Tiago de Freitas Lima";
		mySimpleFormObject.age = 31;
		myComplexFormObject.simple = mySimpleFormObject;

		FormObject formObject = formObjects.of(MyComplexFormObject.class);

		String expected= "form.whatever_name=Tiago+de+Freitas+Lima&form.whatever_age=31&"
				+ "form.iterable.name=Tiago&form.iterable.name=Freitas&form.iterable.name=Lima&"
				+ "form.prefixed.object.name=Tiago+de+Freitas+Lima&form.prefixed.object.age=31&"
				+ "form.simple.name=Tiago+de+Freitas+Lima&form.simple.age=31";

		assertEquals(expected, formObject.serialize(myComplexFormObject));
	}

	@Form
	private class MySimpleFormObject {

		@Field
		private String name;

		@Field
		private int age;
	}

	@Form
	private class MyIterableFormObject {

		@Field("name")
		private Collection<String> names;

	}

	@Form
	private class MyIndexedIterableFormObject {

		@Field(value = "name", indexed = true)
		private Collection<String> names;

	}

	@Form
	private class MyComplexIndexedIterableFormObject {

		@Field(value = "object", indexed = true)
		private Collection<MySimpleFormObject> objects;

	}

	@Form
	private class MyCustomizedFormObject {

		@Field("whatever_name")
		private String name;

		@Field("whatever_age")
		private int age;
	}

	@Form("object")
	private class MyPrefixedFormObject {

		@Field
		private String name;

		@Field
		private int age;
	}

	@Form
	private class MyNestedFormObject {

		@Field
		private String name;

		@Field
		private MySimpleFormObject nested;
	}

	@Form
	private class MyCustomizedNestedFormObject {

		@Field("whatever")
		private MySimpleFormObject nested;
	}

	@Form("form")
	private class MyComplexFormObject {

		@Field("whatever_name")
		private String name;

		@Field("whatever_age")
		private int age;

		@Field
		private MyIterableFormObject iterable;

		@Field
		private MyPrefixedFormObject prefixed;

		@Field
		private MySimpleFormObject simple;
	}
}
