package view

import model.*
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * This file is relative to the view present in the editor that displays the Json in textual mode.
 *
 * @author Gonçalo Manuel Carvalho Carrasco, n.º 109379.
 */

/**
 * View for displaying the Json in textual mode (not editable).
 *
 * @property model the root Json object.
 * @property jsonArea the area where the Json is written and displayed.
 */
class JsonView(private val model: JsonObject) : JPanel() {

    private val jsonArea = JTextArea()

    init {
        layout = GridLayout()
        jsonArea.tabSize = 2
        jsonArea.text = model.toJsonString
        jsonArea.isEditable = false
        add(jsonArea)

        iterateJsonObject(model)
    }

    /**
     * Iterating the elements of a Json object and placing it as an observable.
     *
     * @param jsonObject the Json object to iterate over.
     */
    private fun iterateJsonObject(jsonObject: JsonObject) {
        addObserverToJsonStructured(jsonObject)
        jsonObject.forEach {
            val element: JsonElement = it.value
            if (element is JsonStructured) {
                if (element is JsonObject) iterateJsonObject(element)
                else if (element is JsonArray) iterateJsonArray(element)
            }
        }
    }

    /**
     * Iterating the elements of a Json array and placing it as an observable.
     *
     * @param jsonArray the Json array to iterate over.
     */
    private fun iterateJsonArray(jsonArray: JsonArray) {
        addObserverToJsonStructured(jsonArray)
        jsonArray.value.forEach {
            if (it is JsonStructured) {
                if (it is JsonObject) iterateJsonObject(it)
                else if (it is JsonArray) iterateJsonArray(it)
            }
        }
    }

    /**
     * Adding an observer to a Json structure (object or array).
     * So when an operation is performed on this structure, the view is updated with the new Json.
     *
     * @param structure the Json structure that is made observable.
     */
    private fun addObserverToJsonStructured(structure: JsonStructured) {
        structure.addObserver(object : JsonElementObserver {
            override fun elementAdded(property: String?, element: JsonElement) {
                jsonArea.text = model.toJsonString
            }

            override fun elementModified(old: JsonElement, new: JsonElement) {
                jsonArea.text = model.toJsonString

                // If a new object is added, it also becomes observable
                if (new is JsonObject)
                    addObserverToJsonStructured(new)
            }

            override fun elementRemoved(element: JsonElement) {
                jsonArea.text = model.toJsonString
            }
        })
    }
}