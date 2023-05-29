import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test Scenario II.
 *
 * @author Gonçalo Manuel Carvalho Carrasco, n.º 109379.
 */
class TestsJsonII {

    private val jsonString = "{\"count\": 7, \"items\": [\"socks\", \"pants\", \"shirts\", \"hats\"], " +
            "\"manufacturer\": {\"name\": \"Molly's Seamstress Shop\", \"id\": 39233, " +
            "\"location\": {\"address\": \"123 Pickleton Dr.\", \"city\": \"Tucson\", \"state\": \"AZ\", \"zip\": 85705}}, " +
            "\"total_price\": \"$393.23\", \"purchase_date\": \"2022-05-30\", \"country\": \"USA\"}"

    // Structuring Json in memory
    private val rootObj = JsonObject()
    private val nItems = JsonNumber(7)
    private val items = JsonArray()
    private val itemOne = JsonString("socks")
    private val itemTwo = JsonString("pants")
    private val itemThree = JsonString("shirts")
    private val itemFour = JsonString("hats")

    private val manufacturer = JsonObject()
    private val manufacturerName = JsonString("Molly's Seamstress Shop")
    private val manufacturerId = JsonNumber(39233)
    private val manufacturerLocation = JsonObject()
    private val locationAddress = JsonString("123 Pickleton Dr.")
    private val locationCity = JsonString("Tucson")
    private val locationState = JsonString("AZ")
    private val locationZip = JsonNumber(85705)

    private val totalPrice = JsonString("$393.23")
    private val purchaseDate = JsonString("2022-05-30")
    private val country = JsonString("USA")


    init {
        rootObj.add("count", nItems)
        rootObj.add("items", items)
        items.add(itemOne)
        items.add(itemTwo)
        items.add(itemThree)
        items.add(itemFour)
        rootObj.add("manufacturer", manufacturer)
        rootObj.add("total_price", totalPrice)
        rootObj.add("purchase_date", purchaseDate)
        rootObj.add("country", country)

        manufacturer.add("name", manufacturerName)
        manufacturer.add("id", manufacturerId)
        manufacturer.add("location", manufacturerLocation)
        manufacturerLocation.add("address", locationAddress)
        manufacturerLocation.add("city", locationCity)
        manufacturerLocation.add("state", locationState)
        manufacturerLocation.add("zip", locationZip)
    }

    // region Phase 1 - "Projeção Textual"
    @Test
    fun `test json to textual format`() {
        val allJson: String = rootObj.toJsonString
        assertEquals(jsonString, allJson)
    }
    //endregion

    // region Phase 1 - "Visitantes"
    @Test
    fun `test getting the values stored in a json property`() {
        // "zip" property values
        var values: Collection<JsonElement> = rootObj.getPropertyValues("zip")
        var expected: List<JsonElement> = listOf(locationZip)
        assertEquals(expected, values)

        // "items" property values
        values = rootObj.getPropertyValues("items")
        expected = listOf(items)
        assertEquals(expected, values)

        // Property that doesn't exist in json
        values = rootObj.getPropertyValues("propriedade não definida")
        assertEquals(emptyList<JsonElement>(), values)
    }

    @Test
    fun `test getting objects with property set`() {
        // Objects with "address" and "state" properties
        var objects: Collection<JsonObject> = rootObj.getObjectsWithProperties(listOf("address", "state"))
        var expected: Collection<JsonObject> = listOf(manufacturerLocation)
        assertEquals(expected, objects)

        // Objects with "name", "id" and "location" properties
        objects = rootObj.getObjectsWithProperties(listOf("name", "id", "location"))
        expected = listOf(manufacturer)
        assertEquals(expected, objects)

        // Objects with "items" and "name" properties
        objects = rootObj.getObjectsWithProperties(listOf("items", "name"))
        assertEquals(emptyList<JsonObject>(), objects)

        // Objects with property that doesn't exist in json
        objects = rootObj.getObjectsWithProperties(listOf("propriedade não definida"))
        assertEquals(emptyList<JsonObject>(), objects)

        // Passing a list without properties
        objects = rootObj.getObjectsWithProperties(listOf())
        assertEquals(emptyList<JsonObject>(), objects)
    }

    @Test
    fun `test if property always has the same data type`() {
        // Check if the "items" property is always of list type
        assertTrue(rootObj.propertyHasSameDataType("items", ArrayList::class))

        // Check that the "total_price" property is not of double type
        assertFalse(rootObj.propertyHasSameDataType("total_price", Double::class))

        // Check that the "location" property is always of object type
        assertTrue(rootObj.propertyHasSameDataType("location", LinkedHashMap::class))
    }

    @Test
    fun `test if property is array and all objects have the same structure`() {
        // Check if the items of the "items" property array have the same structure
        assertTrue(items.itemsHaveSameStructure)

        // Adding a new item to the array, of a different data type
        val itemFive = JsonNull()
        items.add(itemFive)
        assertFalse(items.itemsHaveSameStructure)
    }
    //endregion

    // region Phase 2 - "Funcionalidade"
    data class Order(val count: Int, val items: List<String>, val manufacturer: Map<String, Any?>, val total_price: String,
                     val purchase_date: String, val country: String)

    @Test
    fun `test automatic json instantiation`() {
        val items = listOf("socks", "pants", "shirts", "hats")
        val order = Order(7, items, mapOf(
            Pair("name", "Molly's Seamstress Shop"), Pair("id", 39233), Pair("location",
                mapOf(Pair("address", "123 Pickleton Dr."), Pair("city", "Tucson"), Pair("state", "AZ"), Pair("zip", 85705))
        )), "$393.23", "2022-05-30", "USA")

        // Check if properties exist in json object
        val jsonFactory = JsonFactory()
        val rootObj: JsonObject = jsonFactory.createJson(order) as JsonObject
        val expected: Set<String> = setOf("count", "items", "manufacturer", "total_price", "purchase_date", "country")
        assertEquals(expected, rootObj.value.keys)

        // Check the value of properties "count" and "country", and number of "items"
        assertEquals(7, rootObj.value["count"]!!.value)
        assertEquals("USA", rootObj.value["country"]!!.value)
        assertEquals(4, (rootObj.value["items"]!!.value as ArrayList<*>).size)

        // Check the "id" and "city" property in the manufacturer object
        val manufacturer: Map<String, JsonElement> = (rootObj.value["manufacturer"] as JsonObject).value
        assertEquals(39233, manufacturer["id"]!!.value)
        val location: JsonObject = manufacturer["location"] as JsonObject
        assertEquals("Tucson", location.value["city"]!!.value)

        // Check all Json
        assertEquals(jsonString, rootObj.toJsonString)

        // Testing the instantiation of simpler data elements (array and string)
        val jsonArray: JsonArray = jsonFactory.createJson(items) as JsonArray
        assertEquals(4, jsonArray.value.size)
        val item: JsonElement = jsonArray.value[0]
        assertTrue(item is JsonString)
        assertEquals("socks", item.value)
    }
    //endregion

    // region Phase 2 - "Anotações"

    data class OrderWithAnnotations(
        @CustomId("n-items")
        @ToJsonString
        val count: Int,
        @ToJsonString
        val items: List<String>,
        @Exclude
        val manufacturer: Map<String, Any?>,
        @Exclude
        val total_price: String,
        @CustomId("date")
        val purchase_date: String,
        @Exclude
        val country: String)
    private val orderWithAnnotations = OrderWithAnnotations(7, listOf("socks", "pants", "shirts", "hats"), mapOf(
        Pair("name", "Molly's Seamstress Shop"), Pair("id", 39233), Pair("location",
            mapOf(Pair("address", "123 Pickleton Dr."), Pair("city", "Tucson"), Pair("state", "AZ"), Pair("zip", 85705))
        )), "$393.23", "2022-05-30", "USA")

    @Test
    fun `test deletion of properties from instantiation`() {
        // Verification that the "manufacturer", "total_price" and "country" properties have been deleted
        val json: JsonObject = JsonFactory().createJson(orderWithAnnotations) as JsonObject
        val notExpected: Set<String> = setOf("manufacturer", "total_price", "country")
        assert(notExpected.none { it in json.value.keys })
    }

    @Test
    fun `test the use of custom identifiers`() {
        // Verification that only "count" and "purchase_date" properties have custom identifiers
        val json: JsonObject = JsonFactory().createJson(orderWithAnnotations) as JsonObject
        val expected: Set<String> = setOf("n-items", "items", "date")
        assertEquals(expected, json.value.keys)
    }

    @Test
    fun `test the forcing of considering values as Json strings`() {
        val json: JsonObject = JsonFactory().createJson(orderWithAnnotations) as JsonObject

        // Verification that the properties "count" (identifier "n-items") and "items" were mapped as a JsonString
        val nItems: JsonElement = json.value["n-items"]!!
        assertTrue(nItems::class == JsonString::class && nItems.value == "7")
        val items: JsonElement = json.value["items"]!!
        println(items.value)
        assertTrue(items::class == JsonString::class && items.value == "[socks, pants, shirts, hats]")
    }
    //endregion
}