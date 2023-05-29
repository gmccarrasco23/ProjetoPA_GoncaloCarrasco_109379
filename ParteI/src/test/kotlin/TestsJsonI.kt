import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.InvalidKeyException

/**
 * Test Scenario I.
 *
 * @author Gonçalo Manuel Carvalho Carrasco, n.º 109379.
 */
class TestsJsonI {

    private val jsonString = "{\"uc\": \"PA\", \"ects\": 6.0, \"data-exame\": null, \"inscritos\": " +
            "[{\"numero\": 101101, \"nome\": \"Dave Farley\", \"internacional\": true}, " +
            "{\"numero\": 101102, \"nome\": \"Martin Fowler\", \"internacional\": true}, " +
            "{\"numero\": 26503, \"nome\": \"Gonçalo Carrasco\", \"internacional\": false}]}"

    // Structuring Json in memory
    private val rootObj = JsonObject()
    private val curricularUnit = JsonString("PA")
    private val nCredits = JsonNumber(6.0)
    private val examDate = JsonNull()
    private val enrolledStudents = JsonArray()

    private val studentOneData = JsonObject()
    private val studentOneNumber = JsonNumber(101101)
    private val studentOneName = JsonString("Dave Farley")
    private val studentOneIsInternational = JsonBoolean(true)

    private val studentTwoData = JsonObject()
    private val studentTwoNumber = JsonNumber(101102)
    private val studentTwoName = JsonString("Martin Fowler")
    private val studentTwoIsInternational = JsonBoolean(true)

    private val studentThreeData = JsonObject()
    private val studentThreeNumber = JsonNumber(26503)
    private val studentThreeName = JsonString("Gonçalo Carrasco")
    private val studentThreeIsInternational = JsonBoolean(false)

    init {
        rootObj.add("uc", curricularUnit)
        rootObj.add("ects", nCredits)
        rootObj.add("data-exame", examDate)
        rootObj.add("inscritos", enrolledStudents)

        enrolledStudents.add(studentOneData)
        studentOneData.add("numero", studentOneNumber)
        studentOneData.add("nome", studentOneName)
        studentOneData.add("internacional", studentOneIsInternational)
        enrolledStudents.add(studentTwoData)
        studentTwoData.add("numero", studentTwoNumber)
        studentTwoData.add("nome", studentTwoName)
        studentTwoData.add("internacional", studentTwoIsInternational)
        enrolledStudents.add(studentThreeData)
        studentThreeData.add("numero", studentThreeNumber)
        studentThreeData.add("nome", studentThreeName)
        studentThreeData.add("internacional", studentThreeIsInternational)
    }

    // region Phase 1 - "Projeção Textual"
    @Test
    fun `test value of element`() {
        assertTrue(enrolledStudents.value.contains(studentOneData))
        assertTrue(enrolledStudents.value.contains(studentThreeData))
        assertFalse(enrolledStudents.value.contains(curricularUnit))

        assertTrue(rootObj.value.values.any { it == nCredits })
        assertTrue(rootObj.value.values.contains(examDate))
    }

    @Test
    fun `test json to textual format`() {
        // Check one of the objects belonging to the Json
        val personOneJson: String = studentOneData.toJsonString
        val expected = "{\"numero\": 101101, \"nome\": \"Dave Farley\", \"internacional\": true}"
        assertEquals(expected, personOneJson)

        // Check all Json
        assertEquals(jsonString, rootObj.toJsonString)
    }
    //endregion

    // region Phase 1 - "Visitantes"
    @Test
    fun `test getting the values stored in a json property`() {
        // "numero" property values
        var values: Collection<JsonElement> = rootObj.getPropertyValues("numero")
        var expected: Collection<JsonElement> = listOf(studentOneNumber, studentTwoNumber, studentThreeNumber)
        assertEquals(expected, values)

        // "numero" property values
        values = rootObj.getPropertyValues("data-exame")
        expected = listOf(examDate)
        assertEquals(expected, values)

        // Property that doesn't exist in json
        values = rootObj.getPropertyValues("propriedade não definida")
        assertEquals(emptyList<JsonElement>(), values)
    }

    @Test
    fun `test getting objects with property set`() {
        // Objects with "nome" and "numero" properties
        var objects: Collection<JsonObject> = rootObj.getObjectsWithProperties(listOf("nome", "numero"))
        var expected: Collection<JsonObject> = listOf(studentOneData, studentTwoData, studentThreeData)
        assertEquals(expected, objects)

        // Objects with "internacional" property
        objects = rootObj.getObjectsWithProperties(listOf("internacional"))
        expected = listOf(studentOneData, studentTwoData, studentThreeData)
        assertEquals(expected, objects)

        // Objects with "inscritos" property
        objects = rootObj.getObjectsWithProperties(listOf("inscritos"))
        expected = listOf(rootObj)
        assertEquals(expected, objects)

        // Objects with property that doesn't exist in json
        objects = rootObj.getObjectsWithProperties(listOf("propriedade não definida"))
        assertEquals(emptyList<JsonObject>(), objects)

        // Passing a list without properties
        objects = rootObj.getObjectsWithProperties(listOf())
        assertEquals(emptyList<JsonObject>(), objects)
    }

    @Test
    fun `test if property always has the same data type`() {
        // Check if the "numero" property is always of integer type
        assertTrue(rootObj.propertyHasSameDataType("numero", Int::class))

        // Addition of a new item in the JSON, this time with the "numero" property with a value of type Double
        val personFourData = JsonObject()
        personFourData.add("numero", JsonNumber(101010.2))
        enrolledStudents.add(personFourData)
        assertFalse(rootObj.propertyHasSameDataType("numero", Int::class))

        // Check that the "internacional" property is not of string type
        assertFalse(rootObj.propertyHasSameDataType("internacional", String::class))

        // Check that the "data-exame" property is not of string type (it's null)
        assertFalse(rootObj.propertyHasSameDataType("data-exame", String::class))
    }

    @Test
    fun `test if the items of an array property contain the same structure`() {
        // Check if the objects of the "inscritos" property array have the same structure
        assertTrue(enrolledStudents.itemsHaveSameStructure)

        // Adding a new object to the array, with only the "number" property
        val personFourData = JsonObject()
        personFourData.add("numero", JsonNumber(101010))
        enrolledStudents.add(personFourData)
        assertFalse(enrolledStudents.itemsHaveSameStructure)
    }
    //endregion

    // region Phase 2 - "Funcionalidade"
    data class CurricularUnit(val uc: String, val ects: Number, val `data-exame`: String?, val inscritos: List<Map<String, Any?>>)

    @Test
    fun `test automatic json instantiation`() {
        val enrolledStudentsData: List<Map<String, Any?>> = listOf(
            mapOf(Pair("numero", 101101), Pair("nome", "Dave Farley"), Pair("internacional", true)),
            mapOf(Pair("numero", 101102), Pair("nome", "Martin Fowler"), Pair("internacional", true)),
            mapOf(Pair("numero", 26503), Pair("nome", "Gonçalo Carrasco"), Pair("internacional", false))
        )
        val curricularUnit = CurricularUnit("PA", 6.0, null, enrolledStudentsData)

        // Check if properties exist in json object
        val jsonFactory = JsonFactory()
        val rootObj: JsonElement = jsonFactory.createJson(curricularUnit)
        val expected: Set<String> = setOf("uc", "ects", "data-exame", "inscritos")
        assertTrue(rootObj is JsonObject)
        assertEquals(expected, (rootObj as JsonObject).value.keys)

        // Check the value of properties "uc", "ects" and "data-exam"
        assertEquals("PA", rootObj.value["uc"]!!.value)
        assertEquals(6.0, rootObj.value["ects"]!!.value)
        assertNull(rootObj.value["data-exame"]!!.value)

        // Check the number of enrolled students and the value of the "nome" property of one of the enrolled students
        val enrolledStudents: MutableList<JsonElement> = (rootObj.value["inscritos"] as JsonArray).value
        assertEquals(3, enrolledStudents.size)
        var student: JsonObject = enrolledStudents[1] as JsonObject
        assertEquals("Martin Fowler", student.value["nome"]!!.value)

        // Check all Json
        assertEquals(jsonString, rootObj.toJsonString)

        // Testing the instantiation of simpler data elements (array and object)
        val jsonArray: JsonArray = jsonFactory.createJson(enrolledStudentsData) as JsonArray
        assertEquals(3, jsonArray.value.size)
        student = jsonArray.value[2] as JsonObject
        assertEquals("Gonçalo Carrasco", student.value["nome"]!!.value)

        // Testing the instantiation of simpler data elements (integer, string and null value)
        assertTrue(jsonFactory.createJson(10) is JsonNumber)
        assertTrue(jsonFactory.createJson("Gonçalo") is JsonString)
        assertTrue(jsonFactory.createJson(null) is JsonNull)
    }

    inner class ClassWithoutData
    @Test
    fun `test whether to pass an object of a non-data class`() {
        val obj = ClassWithoutData()
        val json: JsonElement = JsonFactory().createJson(obj)
        assertTrue(json is JsonNull)
    }
    // endregion

    // region Phase 2 - "Anotações"
    data class CurricularUnitWithAnnotations(
        @Exclude
        val uc: String,
        @CustomId("n-creditos")
        val ects: Number,
        @ToJsonString
        val `data-exame`: String?,
        @CustomId("alunos-inscritos")
        @ToJsonString
        val inscritos: List<Map<String, Any?>>)

    private val curricularUnitWithAnnotations = CurricularUnitWithAnnotations("PA", 6.0, null, listOf(
        mapOf(Pair("numero", 101101), Pair("nome", "Dave Farley"), Pair("internacional", true)),
        mapOf(Pair("numero", 101102), Pair("nome", "Martin Fowler"), Pair("internacional", true)),
        mapOf(Pair("numero", 26503), Pair("nome", "Gonçalo Carrasco"), Pair("internacional", false))
    ))

    @Test
    fun `test deletion of properties from instantiation`() {
        // Verification that the "uc" property has been deleted
        val json: JsonObject = JsonFactory().createJson(curricularUnitWithAnnotations) as JsonObject
        assert(!json.value.keys.contains("uc"))
    }

    @Test
    fun `test the use of custom identifiers`() {
        // Check what custom identifier of "ects" and "inscritos" properties are "n-creditos" and "alunos-inscritos"
        val json: JsonObject = JsonFactory().createJson(curricularUnitWithAnnotations) as JsonObject
        val expected: Set<String> = setOf("n-creditos", "alunos-inscritos", "data-exame")
        assertEquals(expected, json.value.keys)

        // Verify that using an empty custom identifier throws an exception
        data class Tmp(@CustomId(" ") val tmp: Int)
        assertThrows<InvalidKeyException> { JsonFactory().createJson(Tmp(0)) }
    }

    @Test
    fun `test the forcing of considering values as Json strings`() {
        val json: JsonObject = JsonFactory().createJson(curricularUnitWithAnnotations) as JsonObject

        // Verification that the properties "data-exam" and "inscritos" (identifier "alunos-inscritos") were mapped as a JsonString
        val examDate: JsonElement = json.value["data-exame"]!!
        assertTrue(examDate::class == JsonString::class && examDate.value == "null")
        val enrolledStudents: JsonElement = json.value["alunos-inscritos"]!!
        assertTrue(enrolledStudents::class == JsonString::class)

        // Verification that the "ects" property (identifier "n-credits") continues to be mapped as JsonNumber
        val ects: JsonElement = json.value["n-creditos"]!!
        assertTrue(ects::class == JsonNumber::class && ects.value == 6.0)
    }
    //endregion
}