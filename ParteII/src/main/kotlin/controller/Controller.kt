package controller

import view.JsonEditor
import view.JsonEditorObserver
import model.*
import view.JsonView
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * This file is relative to the controller of the entire application, according to the MVC pattern.
 * This is where the application is started.
 *
 * @author Gonçalo Manuel Carvalho Carrasco, n.º 109379.
 */

/**
 * Class representing the Json editor.
 * On the left side of the editor the user can modify the Json. On the right side the Json is displayed.
 *
 * @property model the Json object initially displayed in the editor.
 * @property frame the window corresponding to the whole editor.
 */
class Editor(private val model: JsonObject) {

    private val frame = JFrame("JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 2)
        size = Dimension(900, 600)
        setLocationRelativeTo(null)

        // Commands stack
        val commands: MutableList<Command> = mutableListOf()

        val editor = JsonEditor(model)
        editor.addObserver(object : JsonEditorObserver {
            override fun elementAdded(property: String?, element: JsonElement, parent: JsonStructured) {
                val command: Command = AddCommand(property, element, parent)
                commands.add(command)
                command.run()
            }

            override fun elementModified(old: JsonElement, new: JsonElement) {
                val command: Command = EditCommand(old, new)
                commands.add(command)
                command.run()
            }

            override fun elementRemoved(property: String?, element: JsonElement) {
                val command: Command = RemoveCommand(property, element)
                commands.add(command)
                command.run()
            }
        })

        // Creating and configuring the behavior of the undo button
        val undoBtn = JButton("Undo")
        undoBtn.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (commands.isNotEmpty())
                    commands.removeLast().undo()
            }
        })

        // Adding views to the frame - Json editor and viewer
        val jsonEditorPanel: JPanel = JPanel().apply {
            add(undoBtn)
            add(editor)
        }
        add(jsonEditorPanel)
        add(JsonView(model))
    }

    /**
     * Make the editor visible to the user.
     */
    fun open() {
        frame.isVisible = true
    }
}

/**
 * Creating the default Json object to be used initially.
 *
 * @return the Json object created and structured.
 */
fun structureJson(): JsonObject {
    val rootObj = JsonObject()
    rootObj.add("uc", JsonString("PA"))
    rootObj.add("ects", JsonNumber(6.0))
    rootObj.add("data-exame", JsonNull())
    val enrolledStudents = JsonArray()

    val studentThreeOne = JsonObject()
    studentThreeOne.add("numero", JsonNumber(26503))
    studentThreeOne.add("nome", JsonString("Gonçalo Carrasco"))
    studentThreeOne.add("internacional", JsonBoolean(false))
    enrolledStudents.add(studentThreeOne)

    val studentTwoData = JsonObject()
    studentTwoData.add("numero", JsonNumber(101102))
    studentTwoData.add("nome", JsonString("Martin Fowler"))
    studentTwoData.add("internacional", JsonBoolean(true))
    enrolledStudents.add(studentTwoData)

    val studentThreeData = JsonObject()
    studentThreeData.add("numero", JsonNumber(101101))
    studentThreeData.add("nome", JsonString("Dave Farley"))
    studentThreeData.add("internacional", JsonBoolean(true))
    enrolledStudents.add(studentThreeData)
    rootObj.add("inscritos", enrolledStudents)

    val courses = JsonArray()
    courses.add(JsonString("MEI"))
    courses.add(JsonString("MIG"))
    courses.add(JsonString("METI"))
    rootObj.add("cursos", courses)

    return rootObj
}

/**
 * Application starting point.
 */
fun main() {
    // Structuring Json in memory
    val rootObj: JsonObject = structureJson()

    // Displays the editor
    Editor(rootObj).open()
}