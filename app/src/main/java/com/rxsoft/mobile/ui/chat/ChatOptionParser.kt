package com.rxsoft.mobile.ui.chat

/**
 * Parses bot option menus of the form:
 *
 *     Please select an action
 *     CODE: Label
 *     CODE2: Another label
 *
 * The same format the storefront chatbot renders as tappable buttons.
 */
object ChatOptionParser {
    data class ParsedQuestionOptions(
        val title: String,
        val options: List<Option>,
    )

    data class Option(val value: String, val label: String)

    fun parse(text: String): ParsedQuestionOptions? {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size < 2) return null

        val title = lines.first()
        val optionLines = lines.drop(1).filter { it.contains(":") }
        if (optionLines.isEmpty()) return null

        val options = mutableListOf<Option>()
        for (line in optionLines) {
            val clean = line.replace("\u200B", "").trim()
            val idx = clean.indexOf(':')
            if (idx == -1) continue
            val value = clean.substring(0, idx).trim()
            val label = clean.substring(idx + 1).trim()
            if (value.isEmpty() || label.isEmpty()) continue
            options.add(Option(value, label))
        }

        return if (options.isNotEmpty()) {
            ParsedQuestionOptions(title, options)
        } else {
            null
        }
    }
}
