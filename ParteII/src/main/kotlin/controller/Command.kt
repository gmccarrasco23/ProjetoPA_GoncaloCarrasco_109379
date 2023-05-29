package controller

import model.JsonArray
import model.JsonElement
import model.JsonObject
import model.JsonStructured

/**
 * This file is relative to the implementation of the "Commands" design pattern.
 * Implementation to allow undo functionality in the editor.
 *
 * @author Gonçalo Manuel Carvalho Carrasco, n.º 109379.
 */

/**
 * Interface for implementing the "Commands" design pattern.
 */
interface Command {
    fun run()
    fun undo()
}

/**
 * Command that represents the addition of an element to the Json.
 *
 * @property property the name of the element property. Can be "null" if the element is added to an array.
 * @property element the element to add to the model.
 * @property parent the Json element that is the parent of the added element.
 */
class AddCommand(private val property: String?, private val element: JsonElement, private val parent: JsonStructured) : Command {

    /**
     * Execution of the command and consequent addition of the element to the Json.
     */
    override fun run() {
        if (parent is JsonArray) parent.add(element)
        else if (parent is JsonObject) parent.add(property!!, element)
    }

    /**
     * Undoing the addition of the element, i.e., removal.
     */
    override fun undo() {
        parent.remove(element)
    }
}

/**
 * Command that represents the modification of a Json element.
 *
 * @property old the Json element before it was modified.
 * @property new the resulting Json element.
 */
class EditCommand(private val old: JsonElement, private val new: JsonElement) : Command {

    /**
     * Execution of the command and consequent modification of the Json element.
     */
    override fun run() {
        val elementParent: JsonStructured? = old.parent
        elementParent?.modify(old, new)
    }

    /**
     * Undo the element change, that is, return to the old element.
     */
    override fun undo() {
        val elementParent: JsonStructured? = new.parent
        elementParent?.modify(new, old)
    }
}

/**
 * Command that represents the removal of a Json element.
 *
 * @property property the name of the element property. It can be "null" if the element to be removed belongs to an array.
 * @property element the element to remove from the model.
 */
class RemoveCommand(private val property: String?, private val element: JsonElement): Command {

    /**
     * Execution of the command and consequent removal of the Json element.
     */
    override fun run() {
        val elementParent: JsonStructured? = element.parent
        elementParent?.remove(element)
    }

    /**
     * Undo the removal of the element, that is, add it back in.
     */
    override fun undo() {
        val parent: JsonStructured? = element.parent
        if (parent is JsonArray) parent.add(element)
        else if (parent is JsonObject) parent.add(property!!, element)
    }
}