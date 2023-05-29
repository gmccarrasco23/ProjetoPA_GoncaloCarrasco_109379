package view

import model.*
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.Border

/**
 * This file is relative to the part of the editor that allows you to modify the Json.
 * You can add properties, edit elements or even remove them.
 *
 * @author Gonçalo Manuel Carvalho Carrasco, n.º 109379.
 */

/**
 * Class corresponds to the area of the editor that allows the user to modify the Json.
 * The controls corresponding to the Json elements present in this view are of type Widget.
 *
 * @property model the root Json object.
 * @property observers the observers of the operations performed in the editor on the Json.
 * @property scrollPane the panel with horizontal and vertical scroll.
 * @property panel the panel embedded in ScrollPane where the components for modifying the Json are placed.
 * @property arrayWasChanged the variable that indicates whether a Json array has changed.
 */
class JsonEditor(private val model: JsonObject) : JPanel() {
    
    private val observers: MutableList<JsonEditorObserver> = mutableListOf()
    private var scrollPane: JScrollPane
    private var panel: JPanel
    private var arrayWasChanged: Boolean = false

    init {
        layout = GridLayout()
        border = BorderFactory.createEmptyBorder(10, 50, 0, 50)

        panel = createPanel()
        scrollPane = JScrollPane(panel).apply {
         horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
         verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        }
        add(scrollPane)
        addObserverToJsonStructured(model)
    }

    /**
     * Adding an observer to observe operations performed on a particular Json structure (object or array).
     * It responds to the operations performed on the model (adding, editing and removing) to update the GUI accordingly.
     *
     * @param structure the Json structure that will be observed.
     */
    private fun addObserverToJsonStructured(structure: JsonStructured) {
        structure.addObserver(object : JsonElementObserver {
            override fun elementAdded(property: String?, element: JsonElement) {
                arrayWasChanged = true
                revalidate()
                repaint()
            }

            override fun elementModified(old: JsonElement, new: JsonElement) {
                replaceElement(old, new)
            }

            override fun elementRemoved(element: JsonElement) {
                removeElement(element)
            }
        })
    }

    /**
     * Method to, when a Json array is changed, redraw the entire contents.
     */
    override fun paintComponent(g: Graphics) {
        if (arrayWasChanged) {
            panel = createPanel()
            scrollPane.setViewportView(panel)
            arrayWasChanged = false
        }
    }

    /**
     * Method to change an editor control resulting from a Json edit.
     *
     * @param old the Json element before it is edited.
     * @param new the resulting Json element.
     */
    private fun replaceElement(old: JsonElement, new: JsonElement) {
        if (new is JsonObject && old !is JsonObject) {
            // If the new element is an object, it means that a new object has been added to an array.
            // Redesign of all content.
            arrayWasChanged = true
            revalidate()
            repaint()
        }
        else {
            val widget: Widget? = findWidgetAndParentPanel(old)?.first
            widget?.let {
                widget.modify(new)
            }
        }
    }

    /**
     * Method to remove an editor element resulting from a Json removal.
     *
     * @param element the Json element removed.
     */
    private fun removeElement(element: JsonElement) {
        if (element is JsonObject) {
            // If the removed element is a Json object.
            if (element.value.isNotEmpty()) {
                // If this object has content, remove all elements contained in it.
                val children: JsonElement = element.value.values.first()
                val widgetAndPanel: Pair<Widget, JPanel>? = findWidgetAndParentPanel(children)
                widgetAndPanel?.let {
                    val targetPanel: JPanel = it.second
                    removePanelFromContainer(targetPanel, targetPanel.parent)
                }
            } else {
                // If the object has no Json elements, redraw the contents of the view.
                arrayWasChanged = true
            }
        } else {
            val widgetAndPanel: Pair<Widget, JPanel>? = findWidgetAndParentPanel(element)
            widgetAndPanel?.let {
                removePanelFromContainer(it.first, it.second)
            }
        }
    }

    /**
     * Removing a panel present in a certain view container.
     *
     * @param target the panel to be removed.
     * @param parent the parent container of the panel.
     */
    private fun removePanelFromContainer(target: JPanel, parent: Container) {
        parent.remove(target)
        parent.revalidate()
        parent.repaint()
    }

    /**
     * Searching for the widget corresponding to a given Json element.
     *
     * @param element the Json element that the search is performed with.
     * @return if found, the widget and the panel in which the widget is found, otherwise it returns "null".
     */
    private fun findWidgetAndParentPanel(element: JsonElement): Pair<Widget, JPanel>? {
        var pair: Pair<Widget, JPanel>? = null

        fun aux(element: JsonElement, panel: JPanel = this.panel) {
            panel.components.forEach {
                if (pair == null && it is Widget && it.matches(element)) {
                    pair = Pair(it, panel)
                    return
                }
                else if (it is JPanel) aux(element, it)
            }
        }

        aux(element)
        return pair
    }

    /**
     * Creating the panel with all the components that correspond to the Json elements and that allows you to modify the Json.
     * The root object (model) is iterated to create the components.
     *
     * @return the panel that allows you to modify the Json.
     */
    private fun createPanel(): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            model.forEach {
                when (val value: JsonElement = it.value) {
                    is JsonArray -> iterateJsonArray(it.key, value, this)
                    else -> add(Widget(it.key, it.value))
                }
            }
        }

    /**
     * Creating the panel and its components corresponding to a Json object.
     * A widget is created for each property of the Json object.
     *
     * @param jsonObject the Json object.
     * @return the panel with the necessary components for representing and modifying the Json object.
     */
    private fun iterateJsonObject(jsonObject: JsonObject): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            jsonObject.value.forEach { add(Widget(it.key, it.value)) }

            addObserverToJsonStructured(jsonObject)
        }

    /**
     * Creating the panel and its components corresponding to a Json array.
     * The array is iterated and widgets are generated according to the elements present in it (primitive elements or Json objects).
     *
     * @param property the name of the property that holds the array.
     * @param jsonArray the Json array.
     * @param panel the panel where the component referring to the name of the Json array property is placed.
     */
    private fun iterateJsonArray(property: String, jsonArray: JsonArray, panel: JPanel): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT

            val objectsPanel = JPanel()
            val outerBorder: Border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
            objectsPanel.layout = GridLayout(jsonArray.value.size, 1)

            // Generating the components for each element of the array
            jsonArray.value.forEachIndexed { index, element ->
                val elementPanel: JPanel
                if (element is JsonObject) {
                    // If the given array element is an object
                    elementPanel = iterateJsonObject(element)

                    // Applying the border to the Json object panel
                    elementPanel.border = BorderFactory.createCompoundBorder(
                        outerBorder,
                        BorderFactory.createLineBorder(if (index % 2 == 0) Color.GRAY else Color.DARK_GRAY, 6)
                    )
                } else {
                    // If the given array element is of type primitive (string, number, etc.)
                    elementPanel = Widget(element = element)
                    elementPanel.border = BorderFactory.createCompoundBorder(
                        outerBorder,
                        BorderFactory.createLineBorder(if (index % 2 == 0) Color.GRAY else Color.DARK_GRAY, 4)
                    )
                }

                // Right-click menu to add or delete array element
                elementPanel.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            val menu = JPopupMenu("Message")
                            val add = JButton("Add")
                            add.addActionListener {
                                observers.forEach { it.elementAdded(element =  JsonNull(), parent =  jsonArray) }
                                menu.isVisible = false
                            }
                            val del = JButton("Delete")
                            del.addActionListener {
                                observers.forEach { it.elementRemoved(element = element) }
                                menu.isVisible = false
                            }
                            menu.add(add)
                            menu.add(del)
                            menu.show(elementPanel, e.x, e.y)
                        }
                    }
                })

                objectsPanel.add(elementPanel)
            }

            // Adding the widget corresponding to the Json array property name
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
                add(Widget(property = property))
                add(objectsPanel)
                panel.add(this)
            }

            addObserverToJsonStructured(jsonArray)
        }

    /**
     * Addition of an observer that reacts to changes in the editor.
     *
     * @param observer the object corresponding to the observer.
     * @return true because the list is always modified.
     */
    fun addObserver(observer: JsonEditorObserver) {
        observers.add(observer)
    }

    /**
     * Class related to widgets added to the editor.
     * It allows you to represent Json elements through.
     * It is possible to create a widget with the property name (label), a field to modify a Json element, or both.
     *
     * @property property the name of the property of a Json element ("null" for creating a widget for an element with no property).
     * @property element  the Json element to represent ("null" when creating a widget for the property name of an array).
     * @property propertyLabel the component related to the property name.
     * @property field the component relative to the field of the Json element (text box, check box, etc., depending on the type of Json element).
     */
    inner class Widget(private val property: String? = null, private var element: JsonElement? = null) : JPanel() {

        private lateinit var propertyLabel: JLabel
        private lateinit var field: JComponent

        init {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT

            // Creating the component corresponding to the property name
            if (property != null) {
                propertyLabel = JLabel(property)
                propertyLabel.border = BorderFactory.createEmptyBorder(5, 5, 0, 5)
                add(propertyLabel)
            }

            // Creating the component corresponding to the Json element
            if (element != null) {
                createField()

                // Right click menu to add new element or delete this one
                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            val add = JButton("Add Property")
                            val menu = JPopupMenu("Message")
                            add.addActionListener {
                                val property: String? = JOptionPane.showInputDialog("Property Name:")
                                if (!property.isNullOrBlank()) {
                                    observers.forEach { it.elementAdded(property, JsonNull(), element!!.parent!!) }
                                }
                                menu.isVisible = false
                            }
                            val del = JButton("Delete \"$property\"")
                            del.addActionListener {
                                observers.forEach { it.elementRemoved(property, element!!) }
                                menu.isVisible = false
                            }
                            menu.add(add)
                            menu.add(del)
                            menu.show(this@Widget, e.x, e.y)
                        }
                    }
                })
            }
        }

        /**
         * Creating the field depending on the type of the Json element.
         */
        private fun createField() {
            if (::field.isInitialized)
                remove(field)

            when (element) {
                is JsonNull -> field = JLabel("N/A")
                is JsonBoolean -> {
                    field = JCheckBox()
                    (field as JCheckBox).isSelected = (element as JsonBoolean).value
                }
                else -> field = JTextField(element!!.value.toString())
            }
            preferredSize = Dimension(150, 25)
            add(field)

            if (field is JLabel) {
                // Listener for if the field value is "null" and is clicked on, the transformation is made into a text box
                field.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        val newElement: JsonElement = JsonString("")
                        observers.forEach { it.elementModified(element!!, newElement) }
                        revalidate()
                        repaint()
                    }
                })
            } else {
                // Listener for the loss of focus of the Json element component, in order to respond to its modification
                field.addFocusListener(object : FocusAdapter() {
                    override fun focusLost(e: FocusEvent) {
                        val newElement: JsonElement

                        if (toCreateJsonObject()) {
                            newElement = JsonObject()
                            val text: String = (field as JTextField).text.trim()
                            val property: String = text.substringBefore(':')
                            newElement.add(property, JsonNull())
                        } else {
                            val value: Any? = getFieldValue(field)

                            // If you do not change the value of the field, the element is not modified
                            if (element!!.value == value)
                                return

                            newElement = JsonFactory().createJson(value)
                        }

                        observers.forEach { it.elementModified(element!!, newElement) }
                    }
                })
            }
        }

        /**
         * Check that all the conditions for changing the field in a new Json object are met.
         *
         * @return true if the conditions are met, false otherwise.
         */
        private fun toCreateJsonObject(): Boolean =
            !::propertyLabel.isInitialized && field is JTextField && (field as JTextField).text.contains(":")

        /**
         * Obtaining the present value in the field.
         *
         * @param component the field.
         * @return the present value in the field.
         */
        private fun getFieldValue(component: JComponent): Any? {
            return if (component is JCheckBox) {
                component.isSelected
            } else {
                var text = ""
                if (component is JTextField) {
                    text = component.text.trim()
                } else if (component is JLabel) {
                    text = component.text.trim()
                }

                if (text.isBlank() || text == "N/A") null
                else if (text == "true" || text == "false") text.toBoolean()
                else if (text.toIntOrNull() != null) text.toInt()
                else if (text.toDoubleOrNull() != null) text.toDouble()
                else text
            }
        }

        /**
         * Modification of the Json element associated with the widget.
         *
         * @param new the new Json element.
         */
        fun modify(new: JsonElement) {
            element = new
            createField()
        }

        /**
         * Checking whether a Json element matches the element associated with the widget.
         *
         * @param e the Json element to compare with the widget's Json element.
         * @return true if it is the same Json element, false otherwise.
         */
        fun matches(e: JsonElement) = element == e
    }
}

/**
 * Interface whose operations represent reactions to observable operations (adding, modifying and removing Json elements).
 * Designed to notify observers of changes made to the Json editor.
 */
interface JsonEditorObserver {

    /**
     * Operation to react to the addition of a Json element.
     *
     * @param property the name of the element property. Can be "null" if the element is added to an array.
     * @param element the added Json element.
     * @param parent the parent element of the Json element to add.
     */
    fun elementAdded(property: String? = null, element: JsonElement, parent: JsonStructured) {}

    /**
     * Operation to react to the modification of a Json element.
     *
     * @param old the Json element before it is modified.
     * @param new the Json element resulting from the modification.
     */
    fun elementModified(old: JsonElement, new: JsonElement) {}

    /**
     * Operation to react to the removal of a Json element.
     *
     * @param property the name of the property of the element to remove, if the element has an associated property.
     * @param element the Json element removed.
     */
    fun elementRemoved(property: String? = null, element: JsonElement) {}
}