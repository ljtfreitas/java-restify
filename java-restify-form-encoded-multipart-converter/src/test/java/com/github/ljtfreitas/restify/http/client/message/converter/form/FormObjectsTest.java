package com.github.ljtfreitas.restify.http.client.message.converter.form;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.converter.form.provided.internal.FormObject;
import com.github.ljtfreitas.restify.http.client.message.converter.form.provided.internal.FormObjects;
import com.github.ljtfreitas.restify.http.contract.form.Form;
import com.github.ljtfreitas.restify.http.contract.form.FormFieldSerializer;
import com.github.ljtfreitas.restify.http.contract.form.Notation;
import com.github.ljtfreitas.restify.http.contract.form.Form.Field;

public class FormObjectsTest {

	private FormObjects formObjects;

	@Before
	public void setup() {
		formObjects = FormObjects.cache();
	}

	@Test
	public void shouldSerializeFormObjectUsingDeclaredFieldNames() {
		MySimpleFormObject form = new MySimpleFormObject();
		form.name = "Tiago de Freitas Lima";
		form.age = 31;

		FormObject formObject = formObjects.of(MySimpleFormObject.class);

		String expected = "name=Tiago+de+Freitas+Lima&age=31";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectOnlyWithNonNullFields() {
		MySimpleFormObject form = new MySimpleFormObject();
		form.age = 31;

		FormObject formObject = formObjects.of(MySimpleFormObject.class);

		String expected = "age=31";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectWithIterableField() {
		MyIterableFormObject form = new MyIterableFormObject();
		form.names = Arrays.asList("Tiago", "Freitas", "Lima");

		FormObject formObject = formObjects.of(MyIterableFormObject.class);

		String expected = "name=Tiago&name=Freitas&name=Lima";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectWithIndexedIterableField() {
		MyIndexedIterableFormObject form = new MyIndexedIterableFormObject();
		form.names = Arrays.asList("Tiago", "Freitas", "Lima");

		FormObject formObject = formObjects.of(MyIndexedIterableFormObject.class);

		String expected = "name[0]=Tiago&name[1]=Freitas&name[2]=Lima";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectWithIndexedIterableOfFormObjectsField() {
		MyComplexIndexedIterableFormObject form = new MyComplexIndexedIterableFormObject();

		MySimpleFormObject simpleForm1 = new MySimpleFormObject();
		simpleForm1.name = "Tiago de Freitas Lima";
		simpleForm1.age = 31;

		MySimpleFormObject simpleForm2 = new MySimpleFormObject();
		simpleForm2.name = "William Shakespeare";
		simpleForm2.age = 25;

		form.objects = Arrays.asList(simpleForm1, simpleForm2);

		FormObject formObject = formObjects.of(MyComplexIndexedIterableFormObject.class);

		String expected = "object[0].name=Tiago+de+Freitas+Lima&object[0].age=31&"
				+ "object[1].name=William+Shakespeare&object[1].age=25";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectUsingCustomizedFieldNames() {
		MyCustomizedFormObject form = new MyCustomizedFormObject();
		form.name = "Tiago de Freitas Lima";
		form.age = 31;

		FormObject formObject = formObjects.of(MyCustomizedFormObject.class);

		String expected = "whatever_name=Tiago+de+Freitas+Lima&whatever_age=31";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectUsingPrefix() {
		MyPrefixedFormObject form = new MyPrefixedFormObject();
		form.name = "Tiago de Freitas Lima";
		form.age = 31;

		FormObject formObject = formObjects.of(MyPrefixedFormObject.class);

		String expected = "object.name=Tiago+de+Freitas+Lima&object.age=31";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectWithNestedField() {
		MyNestedFormObject form = new MyNestedFormObject();
		form.name = "Tiago de Freitas Lima";

		MySimpleFormObject simple = new MySimpleFormObject();
		simple.name = "Tiago de Freitas Lima";
		simple.age = 31;

		form.nested = simple;

		FormObject formObject = formObjects.of(MyNestedFormObject.class);

		String expected = "name=Tiago+de+Freitas+Lima&nested.name=Tiago+de+Freitas+Lima&nested.age=31";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectWithNestedFieldUsingCustomizedName() {
		MyCustomizedNestedFormObject form = new MyCustomizedNestedFormObject();

		MySimpleFormObject nested = new MySimpleFormObject();
		nested.name = "Tiago de Freitas Lima";
		nested.age = 31;

		form.nested = nested;

		FormObject formObject = formObjects.of(MyCustomizedNestedFormObject.class);

		String expected = "whatever.name=Tiago+de+Freitas+Lima&whatever.age=31";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeComplexFormObjectWithMultiplesFields() {
		MyComplexFormObject form = new MyComplexFormObject();
		form.name = "Tiago de Freitas Lima";
		form.age = 31;

		MyIterableFormObject iterableForm = new MyIterableFormObject();
		iterableForm.names = Arrays.asList("Tiago", "Freitas", "Lima");
		form.iterable = iterableForm;

		MyPrefixedFormObject prefixedForm = new MyPrefixedFormObject();
		prefixedForm.name = "Tiago de Freitas Lima";
		prefixedForm.age = 31;
		form.prefixed = prefixedForm;

		MySimpleFormObject simpleForm = new MySimpleFormObject();
		simpleForm.name = "Tiago de Freitas Lima";
		simpleForm.age = 31;
		form.simple = simpleForm;

		MySimpleFormObject simpleForm1 = new MySimpleFormObject();
		simpleForm1.name = "Tiago de Freitas Lima";
		simpleForm1.age = 31;

		MySimpleFormObject simpleForm2 = new MySimpleFormObject();
		simpleForm2.name = "Tiago de Freitas Lima";
		simpleForm2.age = 31;

		form.simples = Arrays.asList(simpleForm1, simpleForm2);

		FormObject formObject = formObjects.of(MyComplexFormObject.class);

		String expected = "form.whatever_name=Tiago+de+Freitas+Lima&form.whatever_age=31&"
				+ "form.iterable.name=Tiago&form.iterable.name=Freitas&form.iterable.name=Lima&"
				+ "form.prefixed.object.name=Tiago+de+Freitas+Lima&form.prefixed.object.age=31&"
				+ "form.simple.name=Tiago+de+Freitas+Lima&form.simple.age=31&"
				+ "form.simples[0].name=Tiago+de+Freitas+Lima&form.simples[0].age=31&"
				+ "form.simples[1].name=Tiago+de+Freitas+Lima&form.simples[1].age=31";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectFieldsUsingCustomSerializer() {
		Date expectedDate = new Date();

		MyCustomizedSerializableFormObject form = new MyCustomizedSerializableFormObject();
		form.date = expectedDate;

		FormObject formObject = formObjects.of(MyCustomizedSerializableFormObject.class);

		String expected = "timestamp=" + expectedDate.getTime();

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectFieldsUsingCustomizedNotationFormat() {
		MyFormWithCustomizedNotationFormat myFormWithCustomizedNotationFormat = new MyFormWithCustomizedNotationFormat();
		myFormWithCustomizedNotationFormat.name = "Tiago de Freitas Lima";

		FormObject formObject = formObjects.of(MyFormWithCustomizedNotationFormat.class);

		String expected = "notation[name]=Tiago+de+Freitas+Lima";

		assertEquals(expected, formObject.serialize(myFormWithCustomizedNotationFormat));
	}

	@Test
	public void shouldSerializeFormObjectFieldsUsingCustomizedNotationDelimiter() {
		MyFormWithCustomizedNotationDelimiter form = new MyFormWithCustomizedNotationDelimiter();
		form.name = "Tiago de Freitas Lima";

		FormObject formObject = formObjects.of(MyFormWithCustomizedNotationDelimiter.class);

		String expected = "notation|name=Tiago+de+Freitas+Lima";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectWithCollectionFieldUsingCustomizedNotationDelimiter() {
		MyFormWithCollectionFieldUsingCustomizedNotationFormat form = new MyFormWithCollectionFieldUsingCustomizedNotationFormat();
		form.names = Arrays.asList("Tiago", "de", "Freitas", "Lima");

		FormObject formObject = formObjects.of(MyFormWithCollectionFieldUsingCustomizedNotationFormat.class);

		String expected = "form<names>[0]=Tiago&form<names>[1]=de&form<names>[2]=Freitas&form<names>[3]=Lima";

		assertEquals(expected, formObject.serialize(form));
	}

	@Test
	public void shouldSerializeFormObjectWithNestedFieldUsingCustomizedNotationDelimiter() {
		MyFormWithNestedFieldUsingCustomizedNotationFormat form = new MyFormWithNestedFieldUsingCustomizedNotationFormat();

		MyNestedFormObject complex = new MyNestedFormObject();
		complex.name = "Tiago de Freitas Lima";

		MySimpleFormObject nested = new MySimpleFormObject();
		nested.name = "Tiago de Freitas Lima";
		nested.age = 33;
		complex.nested = nested;

		form.complex = complex;

		FormObject formObject = formObjects.of(MyFormWithNestedFieldUsingCustomizedNotationFormat.class);

		String expected = "form[complex][name]=Tiago+de+Freitas+Lima&form[complex][nested][name]=Tiago+de+Freitas+Lima&form[complex][nested][age]=33";

		assertEquals(expected, formObject.serialize(form));
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

		@Field(indexed = true)
		private Collection<MySimpleFormObject> simples;
	}

	@Form
	private class MyCustomizedSerializableFormObject {

		@Field(value = "timestamp", serializer = DateTimestampSerializer.class)
		private Date date;
	}

	@Form(value = "notation", notation = @Notation(delimiter = "", prefix = "[", suffix = "]"))
	private class MyFormWithCustomizedNotationFormat {

		@Field
		private String name;
	}

	@Form(value = "notation", notation = @Notation(delimiter = "|"))
	private class MyFormWithCustomizedNotationDelimiter {

		@Field
		private String name;
	}

	@Form(value = "form", notation = @Notation(delimiter = "", prefix = "<", suffix = ">"))
	private class MyFormWithCollectionFieldUsingCustomizedNotationFormat {

		@Field(indexed = true)
		private Collection<String> names;
	}

	@Form(value = "form", notation = @Notation(prefix = "[", suffix = "]", delimiter = ""))
	private class MyFormWithNestedFieldUsingCustomizedNotationFormat {

		@Field
		private MyNestedFormObject complex;
	}

	static class DateTimestampSerializer implements FormFieldSerializer {

		@Override
		public Object serialize(Type type, Object source) {
			return ((Date) source).getTime();
		}
	}
}
