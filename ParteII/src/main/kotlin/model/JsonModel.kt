package model

import kotlin.reflect.KClass

/**
 * This file contains a set of classes and interface that implement the Json data structure.
 *
 * @author Gonçalo Manuel Carvalho Carrasco, n.º 109379.
 */

/**
 * Interface that implements the Json element concept. Any Json property, of whatever type, is consequently a JsonElement.
 *
 * @property value the concrete value of the Json element.
 * @property parent the parent element of the Json element.
 * @property depth the depth of the Json element in the entire Json (e.g., element has depth equal to 1 when the parent is the root object).
 * @property toJsonString the Json element converted to a string.
 * @property observers the observers of the operations performed on a Json element.
 */
sealed interface JsonElement {

    val value: Any?
        get() = null
    var parent: JsonStructured?
    val depth: Int
        get() = parent?.depth?.plus(1) ?: 0
    val toJsonString: String
        get() = value.toString()
    val observers: MutableList<JsonElementObserver>

    /**
     * Identifier accept for the operation that triggers the scan - "Visitor" design pattern.
     *
     * @param v the visitor object that visits all or part of the structure.
     */
    fun accept(v: Visitor)

    /**
     * Adding an observer that reacts to changes in the model.
     *
     * @param observer the object corresponding to the observer.
     * @return true because the list is always modified.
     */
    fun addObserver(observer: JsonElementObserver) = observers.add(observer)

    /**
     * Removing an observer that reacted to changes in the model.
     *
     * @param observer the object corresponding to the observer.
     * @return true if the object is successfully removed, false if it does not exist in the collection.
     */
    fun removeObserver(observer: JsonElementObserver) = observers.remove(observer)
}

/**
 * Interface whose operations represent reactions to observable operations (adding, modifying, and removing Json element).
 */
interface JsonElementObserver {

    /**
     * Operation to react to the addition of a Json element.
     *
     * @param property the name of the element property. Can be "null" if the element is added to an array.
     * @param element the added Json element.
     */
    fun elementAdded(property: String? = null, element: JsonElement) { }

    /**
     * Operation to react to the modification of a Json element.
     *
     * @param old the Json element before it is modified.
     * @param new the Json element resulting from the modification.
     */
    fun elementModified(old: JsonElement, new: JsonElement) { }

    /**
     * Operation to react to the removal of a Json element.
     *
     * @param element the Json element removed.
     */
    fun elementRemoved(element: JsonElement) { }
}

//region Structured Data
/**
 * Class related to a Json structure, i.e. an array or an object.
 *
 * @property observers the observers of the operations performed on a Json structure.
 */
abstract class JsonStructured : JsonElement {
    override val observers: MutableList<JsonElementObserver> = mutableListOf()

    /**
     * Modification of a Json element present in the structure.
     *
     * @param old the old element, before modified.
     * @param new the element resulting from the change.
     */
    abstract fun modify(old: JsonElement, new: JsonElement)

    /**
     * Removal of a Json element present in the structure.
     *
     * @param element the element to remove.
     */
    abstract fun remove(element: JsonElement)
}

/**
 * Alias for easy reference to each entry in the Json object.
 */
typealias ObjectEntry = Map.Entry<String, JsonElement>

/**
 * Class representing a Json object.
 * Implements the Iterable interface to make the Json object iterable.
 *
 * @property value set of pairs, where the key corresponds to the property names and the values to the associated Json elements.
 * @property parent the parent element of the Json object. Can be "null" if the object is the root object of the Json.
 * @property toJsonString the object converted to string, with the default Json object formatting.
 */
class JsonObject : JsonStructured(), Iterable<ObjectEntry> {

    override val value: MutableMap<String, JsonElement> = mutableMapOf()
    override var parent: JsonStructured? = null
    override val toJsonString: String
        get() {
            return value.toList().joinToString(
                prefix = "${if (parent != null && parent !is JsonObject) "\n${"\t".repeat(depth)}" else ""}{\n",
                postfix = "\n${"\t".repeat(depth)}}",
                separator = ",\n")
            { "${"\t".repeat(it.second.depth)}\"${it.first}\": ${it.second.toJsonString}" }
        }

    /**
     * Visit the Json object which is a composite element (contains child elements).
     *
     * @param v the visitor object that visits all or part of the structure.
     */
    override fun accept(v: Visitor) {
        if (v.visit(this))
            value.values.forEach {
                it.accept(v)
            }
        v.endVisit(this)
    }

    /**
     * To make the Json object iterable.
     *
     * @return object of type Iterator to scroll sequentially through the elements present in the Json object.
     */
    override fun iterator(): Iterator<ObjectEntry> = value.iterator()

    /**
     * Adding a property to the Json object.
     * The Json object becomes the parent of this added element and notifies the observers of the addition.
     *
     * @param property the name of the property.
     * @param element the Json element associated with the property.
     */
    fun add(property: String, element: JsonElement) {
        this.value[property] = element
        element.parent = this
        observers.forEach { it.elementAdded(property, element) }
    }

    /**
     * Changing a Json object element.
     * The Json object becomes the parent of the new element and the observers are notified of the change to the element.
     *
     * @param old the old element, before modified.
     * @param new the element resulting from the change.
     */
    override fun modify(old: JsonElement, new: JsonElement) {
        val property: String? = value.entries.find { it.value == old }?.key
        if (property != null) {
            value[property] = new
            new.parent = this
            observers.forEach { it.elementModified(old, new) }
        }
    }

    /**
     * Removing an element from the Json object.
     * Observers are notified of the removal of the element.
     * If the object has been left empty, it is removed from the list of values of its parent element.
     *
     * @param element the element to remove.
     */
    override fun remove(element: JsonElement) {
        val property: String? = value.entries.find { it.value == element }?.key
        if (property != null) {
            value.remove(property)
            observers.forEach { it.elementRemoved(element) }

            if (value.isEmpty())
                parent?.remove(this)
        }
    }

    /**
     * Getting the Json elements associated with a property.
     *
     * @param property the name of the property you are searching for.
     * @return collection with all the Json elements associated with the property.
     */
    fun getPropertyValues(property: String): Collection<JsonElement> {
        val searchByPropertyValues = object: Visitor {
            val propertyValues = mutableListOf<JsonElement>()
            override fun visit(c: JsonStructured): Boolean {
                if (c is JsonObject) {
                    c.value.forEach {
                        if (it.key == property)
                            propertyValues.add(it.value)
                    }
                }

                return true
            }
        }
        this.accept(searchByPropertyValues)
        return searchByPropertyValues.propertyValues
    }

    /**
     * Getting the Json objects that contain a collection of properties.
     *
     * @param properties collection of property names that are checked for existence in each Json object.
     * @return collection with the Json objects that contain these properties.
     */
    fun getObjectsWithProperties(properties: Collection<String>): Collection<JsonObject> {
        val searchByProperties = object: Visitor {
            val objects = mutableListOf<JsonObject>()
            override fun visit(c: JsonStructured): Boolean {
                val tmp: MutableList<String> = properties.toMutableList()
                if (c is JsonObject) {
                    c.value.forEach {
                        if (tmp.contains(it.key))
                            tmp.remove(it.key)
                    }
                    if (tmp.isEmpty()) objects.add(c)
                }

                return true
            }
        }

        if (properties.isNotEmpty()) this.accept(searchByProperties)
        return searchByProperties.objects
    }

    /**
     * Checking whether the value of a given property is always of the same data type.
     *
     * @param property the name of the property you are searching for.
     * @param clazz the class of the data type.
     * @return true if all values of the property are of the data type passed by parameter, false otherwise.
     */
    fun propertyHasSameDataType(property: String, clazz: KClass<*>): Boolean {
        var sameDataType = true
        val checkPropertyDataType = object: Visitor {
            override fun visit(c: JsonStructured): Boolean {
                if (c is JsonObject) {
                    c.value.forEach {
                        if (it.key == property && (it.value.value == null || it.value.value!!::class != clazz)) {
                            sameDataType = false
                            return false
                        }
                    }
                }

                return true
            }
        }
        this.accept(checkPropertyDataType)
        return sameDataType
    }
}

/**
 * Class representing a Json array.
 *
 * @property value vector that holds the Json elements belonging to the array.
 * @property parent the parent element of the Json array.
 * @property toJsonString the array converted to string, with the default Json array formatting.
 * @property itemsHaveSameStructure indicates whether all items in the array contain the same structure.
 */
class JsonArray : JsonStructured() {

    override val value: MutableList<JsonElement> = mutableListOf()
    override var parent: JsonStructured? = null
    override val toJsonString: String
        get() =     value.joinToString(
            prefix = "[",
            postfix = if (value.size > 0 && value.last() is JsonObject) "\n${"\t".repeat(depth)}]" else "]",
            separator = ", ")
        { it.toJsonString }

    val itemsHaveSameStructure: Boolean
        get() {
            val types = value.distinctBy { it::class }
            when (types.size) {
                0 -> return true
                1 -> {
                    if (types[0] is JsonObject) {
                        var objectPropertiesFirstObj: Set<String> = setOf()
                        value.forEachIndexed { index, jsonElement ->
                            if (index == 0) {
                                objectPropertiesFirstObj = (jsonElement as JsonObject).value.keys
                            } else {
                                val objectProperties = (jsonElement as JsonObject).value.keys
                                if (objectProperties != objectPropertiesFirstObj)
                                    return false
                            }
                        }
                    }
                    return true
                }
                else -> return false
            }
        }

    /**
     * Visit the Json array which is a composite element (contains child elements).
     *
     * @param v the visitor object that visits all or part of the structure.
     */
    override fun accept(v: Visitor) {
        if (v.visit(this))
            value.forEach {
                it.accept(v)
            }
        v.endVisit(this)
    }

    /**
     * Adding a Json element to the array.
     * The Json array becomes the parent of this added element and notifies the observers of the addition.
     *
     * @param element the Json element added to the array.
     */
    fun add(element: JsonElement) {
        value.add(0, element)
        element.parent = this
        observers.forEach { it.elementAdded(element = element) }
    }

    /**
     * Changing a Json array element.
     * The Json array becomes the parent of the new element and the observers are notified of the change to the element.
     *
     * @param old the old element, before modified.
     * @param new the element resulting from the change.
     */
    override fun modify(old: JsonElement, new: JsonElement) {
        val index: Int = value.indexOf(old)
        if (index >= 0) {
            value[index] = new
            new.parent = this
            observers.forEach { it.elementModified(old, new) }
        }
    }

    /**
     * Removing an element from the Json array.
     * Observers are notified of the removal of the element.
     *
     * @param element the element to remove.
     */
    override fun remove(element: JsonElement) {
        if (value.remove(element)) {
            observers.forEach { it.elementRemoved(element) }
        }
    }
 }
//endregion

//region Primitive Data

/**
 * Class related to a primitive data type and allowed in the Json structure, i.e. string, numeric and boolean value.
 * The value "null" is also considered.
 *
 * @property observers the observers of the operations performed on a Json primitive.
 * @property parent the parent element of the Json primitive.
 */
abstract class JsonPrimitive : JsonElement {

    override val observers: MutableList<JsonElementObserver> = mutableListOf()
    override var parent: JsonStructured? = null

    /**
     * Visit a Json element of primitive data type, which corresponds to a leaf element of the structure.
     *
     * @param v the visitor object that visits all or part of the structure.
     */
    override fun accept(v: Visitor) {
        v.visit(this)
    }
}

/**
 * Class representing a Json element of boolean data type.
 *
 * @property value true or false.
 */
class JsonBoolean(
    override val value: Boolean
) : JsonPrimitive()

/**
 * Class representing a Json element of numeric data type.
 *
 * @property value the numerical value, with or without decimal places.
 */
class JsonNumber(
    override val value: Number
) : JsonPrimitive()

/**
 * Class representing a Json element of string data type.
 *
 * @property value the string value.
 * @property toJsonString applying the standard formatting of a string in the Json structure (quotation marks delimiting the value).
 */
class JsonString(
    override val value: String
) : JsonPrimitive() {

    override val toJsonString: String
        get() = "\"$value\""
}

/**
 * Class representing an element whose value is "null".
 *
 * @property value always null.
 */
class JsonNull: JsonPrimitive() {

    override val value = null
}
//endregion

//region Visitor
/**
 * Interface corresponding to the visitor - "Visitor" design pattern.
 */
interface Visitor {

    /**
     * Visit operation for the visitable element corresponding to a Json structure (composite element).
     *
     * @param c the json structure element - object or array.
     * @return true if the scan should proceed into the element, false otherwise.
     */
    fun visit(c: JsonStructured): Boolean = true

    /**
     * Operation that signals the end of processing of a compound element.
     */
    fun endVisit(c: JsonStructured) { }

    /**
     * Visit operation for the visitable element corresponding to a Json primitive (leaf element).
     *
     * @param l the json primitive element.
     */
    fun visit(l: JsonPrimitive) { }
}
//endregion

//region Annotations
/**
 * Annotation to exclude a property of a data class from the Json model instantiation.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Exclude

/**
 * Annotation for the use of custom property identifiers.
 *
 * @param identifier the identifier designation.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class CustomId(val identifier: String)

/**
 * Annotation to force values to be considered Json strings.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class ToJsonString
//endregion