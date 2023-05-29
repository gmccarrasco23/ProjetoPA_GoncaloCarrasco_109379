# Biblioteca de Geração de JSON

Aqui estão patentes instruções e tutoriais de como utilizar a biblioteca para a geração e estruturação de JSON.

##  Utilização simples

É fornecido uma série de classes que correspondem a elementos JSON. Esses elementos podem ser dos seguintes tipos: *string*, valor numérico, *booleano*, *null*, matriz e objeto.

```kotlin
private val obj = JsonObject()  
private val shopName = JsonString("Molly's Seamstress Shop")  
private val numberOfItems = JsonNumber(7)
private val items = JsonArray()  
private val dateOfDelivery = JsonNull() 
private val wasPaid = JsonBoolean(true) 
```

Para estruturar e formar o JSON é necessário adicionar ao objeto raiz os elementos que se pretende que dele façam parte, passando também o nome da propriedade de cada um deles:
```kotlin
obj.add("count", nItems)  
obj.add("items", items)
obj.add("was-paid", wasPaid)
obj.add("date-of-delivery", dateOfDelivery)
```

Para estruturar uma matriz, isto é, um *array*, é necessário adicionar também ao mesmo os elementos que se pretende que dele façam parte:
```kotlin
// Elementos JSON
val socks = JsonString("socks")
val pants = JsonString("pants")

// Inserção dos elementos JSON ao array
items.add(socks)
items.add(pants)

// Inserção do array no objeto raiz
obj.add(items)
```

### Outras funcionalidades
É possível obter o valor que cada elemento JSON guarda:
```kotlin
val nItems: Int = numberOfItems.value
val itemsList: List = items.value
```

É possível converter cada elemento JSON para uma cadeia de caracteres, permitindo assim visualizar o JSON a partir desse elemento. 
```kotlin
// Visualização global de todo o JSON
var jsonString: String = obj.toJsonString
println(jsonString)

// Visualização da matriz pertencente ao JSON
jsonString = items.toJsonString
println(items)
```

###  Um exemplo de criação de Json

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
fees.add("comission", comission)

// Estruturação do array da propriedade "descriptors"
val descriptors = JsonArray()
val clean = JsonString("clean")
val fresh = JsonString("fresh")
val juicy = JsonString("juicy")
descriptors.add(clean)
descriptors.add(fresh)
descriptors.add(juicy)

// Inserção de todos os elementos no objeto raiz
obj.add(type)
obj.add(numberOfBeans)
obj.add(numberOfApples)
obj.add(brand)
obj.add(ratio)
obj.add(fees)
obj.add(descriptors)
```


##  *Visitors*


