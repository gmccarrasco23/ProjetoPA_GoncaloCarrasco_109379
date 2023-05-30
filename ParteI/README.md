
# Biblioteca de Geração de JSON

Passam-se a apresentar as instruções, exemplos e tutoriais de como utilizar a biblioteca para a geração e estruturação de JSON.

##  Utilização simples

É fornecido uma série de classes que correspondem a elementos JSON. Esses elementos podem ser dos seguintes tipos: *string*, valor numérico, *booleano*, *null*, matriz e objeto.

```kotlin
val obj = JsonObject()  
val shopName = JsonString("Molly's Seamstress Shop")  
val numberOfItems = JsonNumber(7)
val items = JsonArray()  
val dateOfDelivery = JsonNull() 
val wasPaid = JsonBoolean(true) 
```

Para estruturar e formar o JSON é necessário adicionar ao objeto raiz os elementos que se pretende que dele façam parte, passando também o nome da propriedade de cada um deles:
```kotlin
obj.add("shop-name", shopName)
obj.add("count", numberOfItems)
obj.add("date-of-delivery", dateOfDelivery)
obj.add("was-paid", wasPaid)
```

Para estruturar uma matriz, isto é, um *array*, é necessário adicionar também ao mesmo os elementos que se pretende que dele façam parte:
```kotlin
// Elementos JSON
val socks = JsonString("socks")
val pants = JsonString("pants")

// Inserção dos elementos JSON no array
items.add(socks)
items.add(pants)

// Inserção do array no objeto raiz
obj.add("items", items)
```

### Outras funcionalidades
É possível obter o valor que cada elemento JSON guarda:
```kotlin
val nItems: Number = numberOfItems.value
val itemsList: List<JsonElement> = items.value
```

É possível converter o valor de cada elemento em uma cadeia de caracteres, permitindo visualizar a partir desse elemento o JSON em modo textual. 
```kotlin
// Visualização global do JSON
var jsonString: String = obj.toJsonString
println(jsonString)

// Visualização da matriz pertencente ao JSON
jsonString = items.toJsonString
println(jsonString)
```

###  Criação de um exemplo de JSON

Objeto JSON:
 ```json
{
	"type": "basket",
	"beans": 47,
	"apples": 7,
	"brand": null,
	"ratio": 33.9,
	"fees": {
		"cleaning": "4.50€",
		"baking": "27.30€",
		"commission": "93.10€"
	},
	"descriptors": ["clean", "fresh", "juicy"]
}
```

De seguida apresenta-se como se estrutura este objeto JSON:
```kotlin
// Objeto raiz
val obj = JsonObject()

// Estruturação dos elementos cujo valor é do tipo primitivo
val type = JsonString("basket")
val numberOfBeans = JsonNumber(47)
val numberOfApples = JsonNumber(7)
val brand = JsonNull()
val ratio = JsonNumber(33.9)

// Estruturação do objeto da propriedade "fees"
val fees = JsonObject()
val cleaning = JsonString("4.50€")
val baking = JsonString("27.30€")
val commission = JsonString("93.10€")
fees.add("cleaning", cleaning)
fees.add("baking", baking)
fees.add("commission", commission)

// Estruturação do array da propriedade "descriptors"
val descriptors = JsonArray()
val clean = JsonString("clean")
val fresh = JsonString("fresh")
val juicy = JsonString("juicy")
descriptors.add(clean)
descriptors.add(fresh)
descriptors.add(juicy)

// Inserção de todos os elementos no objeto raiz
obj.add("type", type)
obj.add("beans", numberOfBeans)
obj.add("apples", numberOfApples)
obj.add("brand", brand)
obj.add("ratio", ratio)
obj.add("fees", fees)
obj.add("descriptors", descriptors)
```

##  *Visitor*

A biblioteca oferece a forma de varrimento baseada em visitantes (padrão de desenho *Visitor*). 

### Funcionalidades da biblioteca
A própria biblioteca já tem implementada funcionalidades utilizando este padrão de desenho. 

####  1. Valores guardados em propriedades
É possível obter a partir de um objeto JSON os elementos, e consequentemente os valores guardados, cuja propriedade tem um determinado identificador:
```kotlin
// Elementos JSON cuja propriedade é "apples"
val values: Collection<JsonElement> = obj.getPropertyValues("apples")
```

####  2. Objetos com conjunto de propriedades
 É possível também obter, a partir de um objeto JSON, todos os objetos que têm um conjunto de propriedades:
```kotlin
// Procura de objetos com as propriedades "cleaning" e "baking" 
val objects: Collection<JsonObject> = obj.getObjectsWithProperties(listOf("cleaning", "baking"))
```

####  3. Propriedade com um tipo de dados
 É possível verificar, a partir de um objeto JSON,  se uma propriedade tem sempre o mesmo tipo de dados:
```kotlin
// Verificação se a propriedade "beans" apenas guarda valores inteiros 
val sameDataType: Boolean = obj.propertyHasSameDataType("beans", Int::class)
```

####  4. Matriz com objetos com a mesma estrutura
 É possível verificar, a partir de um *array* JSON,  se os objetos que dele fazem parte têm a mesma estutura:
```kotlin
// Verificação se a matriz "descriptors" guarda objetos todos com a mesma estrutura 
val sameStructure: Boolean = descriptors.itemsHaveSameStructure
```

### Utilização externa do *Visitor*
Também é possível utilizar externamente o visitante como forma de varrimento para realizar determinadas pesquisas ou verificações do JSON que se tenha em memória. 
Assim, passa-se a demonstrar a sua utilização com a finalidade de contar, no objeto raiz e em possíveis "objetos filhos" do JSON, o número de propriedades que guardam valores do tipo *string* (tem-se em conta que o JSON já foi previamente estruturado): 

```kotlin
// Objeto para contagem de elementos do tipo JsonString
val countStringValues = object : Visitor {
    var count = 0
    override fun visit(l: JsonPrimitive) {
        if (l is JsonString)
	        count++
    }
	
	override fun visit(c: JsonStructured): Boolean {  
	    // Visita apenas a objetos Json
	    return c is JsonObject  
	}
}

// Varrimento do objeto JSON e obtenção do número de elementos do tipo string
obj.accept(countStringValues)
val n: Int = countStringValues.count
```

##  Reflexão & Anotações

A biblioteca fornece, por meio de reflexão, uma forma de instanciação de JSON que suporta: objetos de valor (*data class*); coleções (*collection*); tabelas (*map*); tipos primitivos; *strings* e enumerados.
Essa instanciação é realizada através da chamada da função *createJson*, da classe *JsonFactory* desta biblioteca. 

### Instanciação de objetos de valor
Para instanciar objetos de valor, esses objetos têm de ser instâncias de classes de dados (*data class*). De seguida, é fornecido um exemplo de instanciação de um objeto deste tipo:
```kotlin
// Exemplo de classe de dados
data class CurricularUnit(val uc: String, val ects: Number, val dataExame: String?, val inscritos: List<Map<String, Any?>>)

fun main() {
	// Instanciação de objeto da classe de dados "CurricularUnit"
	val enrolledStudentsData: List<Map<String, Any?>> = listOf(
		mapOf(Pair("numero", 101101), Pair("nome", "Dave Farley"), Pair("internacional", true)),  
		mapOf(Pair("numero", 101102), Pair("nome", "Martin Fowler"), Pair("internacional", true)),  
		mapOf(Pair("numero", 26503), Pair("nome", "Gonçalo Carrasco"), Pair("internacional", false)) )  
	val curricularUnit = CurricularUnit("PA", 6.0, null, enrolledStudentsData)

	// Instanciação do JSON
	val obj: JsonElement = JsonFactory().createJson(curricularUnit)
}
```
 
### Instanciação de objetos de valor com anotações
É possível adaptar a instanciação de objetos de valor através da utilização de anotações. A biblioteca fornece as seguintes anotações:
- **@Exclude**: para excluir propriedades da instanciação;
- **@CustomId(*id*)**: para utilizar identificadores personalizados;
- **@ToJsonString**: para forçar que valores sejam considerados *strings* JSON.

De seguida, exemplifica-se a instanciação JSON de um objeto de valor, recorrendo a estas anotações:

```kotlin
// Exemplo de classe de dados
data class CurricularUnitWithAnnotations(  
  @Exclude  
  val uc: String,  
  @CustomId("n-creditos")  
  val ects: Number,  
  @ToJsonString  
  val dataExame: String?,  
  @CustomId("alunos-inscritos") 
  val inscritos: List<Map<String, Any?>>)

fun main() {
	// Instanciação de objeto da classe de dados "CurricularUnitWithAnnotations"
	val curricularUnitWithAnnotations = CurricularUnitWithAnnotations("PA", 6.0, null, listOf(
		mapOf(Pair("numero", 101101), Pair("nome", "Dave Farley"), Pair("internacional", true)),
		mapOf(Pair("numero", 101102), Pair("nome", "Martin Fowler"), Pair("internacional", true))))

	// Instanciação do JSON
	val json: JsonObject = JsonFactory().createJson(curricularUnitWithAnnotations) as JsonObject
}
```

Da instanciação:
- foi excluída a propriedade "uc";
- foram atribuídos às propriedades "ects" e "inscritos", respetivamente, os identificadores personalizados "n-creditos" e "alunos-inscritos"; 
- foram convertidos para *string* os valores da propriedade "dataExame".
 
### Instanciação de outros tipos de dados
De seguida, exemplifica-se a instanciação de coleções, *string* e tipos primitivos:
```kotlin
// Exemplo de coleção
val enrolledStudentsData: List<Map<String, Any?>> = listOf(
	mapOf(Pair("numero", 101101), Pair("nome", "Dave Farley"), Pair("internacional", true)),  
	mapOf(Pair("numero", 101102), Pair("nome", "Martin Fowler"), Pair("internacional", true)))  

// Instanciação da coleção, string e tipos primitivos
val enrolledStudentsArray: JsonArray = JsonFactory().createJson(enrolledStudentsData) as JsonArray
val stringElement: JsonString= JsonFactory().createJson("Dave Farley") as JsonString
val numberElement: JsonNumber = JsonFactory().createJson(10) as JsonNumber
val nullElement: JsonNull = JsonFactory().createJson(null) as JsonNull
```

##  Observadores
Esta biblioteca foi implementada de forma a que os seus elementos fossem observáveis, nomeadamente estruturas JSON, isto é, *JsonObject* e *JsonArray*. Há três operações observáveis que a biblioteca trata de notificar os seus observadores, são elas: quando um elemento é adicionado a uma estrutura JSON; quando um elemento de uma estrutura é modificado, e quando um elemento JSON é removido de uma estrutura.
De seguida, exemplifica-se a utilização dos observadores, ao se colocar um objeto JSON a ser observado:

```kotlin
val obj = JsonObject()
obj.addObserver(object : JsonElementObserver {  
	override fun elementAdded(property: String?, element: JsonElement) {  
	    println("elemento adicionado ao objeto")
	}  
  
    override fun elementModified(old: JsonElement, new: JsonElement) {  
	    println("elemento do objeto foi substituído")
    }
    
    override fun elementRemoved(element: JsonElement) {   
	    println("elemento removido do objeto")
    }  
})

val ucOld = JsonString("PA")
val ucNew = JsonString("PCS")
obj.add("uc", ucOld) // método "elementAdded" executado
obj.modify(ucOld, ucNew) // método "elementModified" executado
obj.remove(ucNew) // método "elementRemoved" executado
```

Assim, sempre que for adicionado um elemento a este objeto, é executado o método *elementAdded* que recebe o nome da propriedade e o elemento que foi adicionado ao objeto. 

Caso seja modificado um elemento do objeto, é executado o método *elementModified* que recebe o elemento JSON anterior à modificação e o novo elemento resultante da modificação. 

Por fim, caso seja removido um elemento do objeto, é executado o método *elementRemoved* que recebe o elemento JSON que foi removido do objeto. 

Desta forma, pode-se codificar e implementar as ações que se pretenda realizar aquando da inserção, modificação e remoção de um elemento de uma estrutura JSON.

Para os *JsonArray* aplica-se a mesma lógica.
