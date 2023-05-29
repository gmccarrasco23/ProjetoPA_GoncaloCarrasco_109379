package model

import java.security.InvalidKeyException
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.full.*

/**
 * This file is related to the automatic instantiation of the model.
 *
 * @author Gonçalo Manuel Carvalho Carrasco, n.º 109379.
 */

/**
 * Class that allows you to automatically instantiate the model.
 * It supports value objects (data class), collections, tables (map), primitive types, strings, and enumerates.
 */
class JsonFactory {

    /**
     * Creating Json, by reflection, from a value object (data class), collection, table, string, enumerated or any primitive type.
     *
     * @param value what is intended to be instantiated.
     * @return the Json root object, if you want to instantiate a value object (data class), or a Json element depending on the type of value passed as parameter.
     */
    fun createJson(value: Any?): JsonElement {
        if (value == null)
            return JsonNull()

        val clazz: KClass<*> = value::class
        val element: JsonElement

        //  If the value passed in corresponds to an instance of a data class
        if (clazz.isData) {
            val dataClassFields: List<KProperty<*>> = clazz.dataClassFields
            element = JsonObject()

            dataClassFields.forEach {
                if (!it.hasAnnotation<Exclude>()) {
                    val classifier: KClass<*>?
                    var propertyValue: Any? = it.call(value)

                    if (it.hasAnnotation<ToJsonString>()) {
                        // If the property has the annotation, it is converted to String
                        classifier = String::class
                        propertyValue = propertyValue.toString()
                    }
                    else {
                        classifier = it.returnType.classifier as KClass<*>
                    }

                    val propertyName: String = if (it.hasAnnotation<CustomId>()) it.findAnnotation<CustomId>()!!.identifier else it.name
                    if (propertyName.isBlank())
                        throw InvalidKeyException("custom identifier for property \"${it.name}\" cannot be blank")

                    val jsonElement: JsonElement = mapJsonElement(propertyValue, classifier)
                    element.add(propertyName, jsonElement)
                }
            }
        } else {
            val classifier: KClass<*> = value::class
            element = mapJsonElement(value, classifier)
        }

        return element
    }

    /**
     * Creating a Json array element from a data collection.
     *
     * @param collection the collection from which the array is structured.
     * @return object of type JsonArray.
     */
    private fun createJsonArray(collection: Collection<*>): JsonArray {
        val jsonArray = JsonArray()
        collection.forEach {
            val jsonElement: JsonElement = if (it != null) mapJsonElement(it, it::class) else JsonNull()
            jsonArray.add(jsonElement)
        }

        return jsonArray
    }

    /**
     * Creating a Json object from a table (map).
     *
     * @param map the table from which the json object is structured.
     * @return object of type JsonObject.
     */
    private fun createJsonObject(map: Map<*, *>): JsonObject {
        val jsonObject = JsonObject()
        map.forEach {
            val jsonElement: JsonElement = if (it.value != null) mapJsonElement(it.value, it.value!!::class) else JsonNull()
            jsonObject.add(it.key as String, jsonElement)
        }

        return jsonObject
    }

    /**
     * Mapping a property in a Json element (e.g. JsonString, JsonNumber, etc.), depending on its value and data type.
     *
     * @param value property value.
     * @param classifier the class of the property (data type).
     * @return the property mapped to a Json element.
     */
    private fun mapJsonElement(value: Any?, classifier: KClass<*>): JsonElement {
        return if (value == null) JsonNull()
        else if (classifier.isSubclassOf(Collection::class)) createJsonArray(value as Collection<*>)
        else if (classifier.isSubclassOf(Map::class)) createJsonObject(value as Map<*, *>)
        else if (classifier.isSubclassOf(Number::class)) JsonNumber(value as Number)
        else if (classifier.isEnum) JsonString(value as String)
        else {
            when (classifier) {
                String::class -> JsonString(value as String)
                Boolean::class -> JsonBoolean(value as Boolean)
                else -> JsonNull()
            }
        }
    }

    /**
     * Get list of attributes in order of primary constructor.
     */
    private val KClass<*>.dataClassFields: List<KProperty<*>>
        get() {
            require(isData) { "instance must be data class" }
            return primaryConstructor!!.parameters.map { p ->
                declaredMemberProperties.find { it.name == p.name }!!
            }
        }

    /**
     * Knowing if a KClassifier is an enumerated.
     */
    private val KClassifier?.isEnum: Boolean
        get() = this is KClass<*> && this.isSubclassOf(Enum::class)
}