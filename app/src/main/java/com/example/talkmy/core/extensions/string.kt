package com.example.talkmy.core.extensions

import org.apache.commons.text.StringEscapeUtils

private val NEW_LINE_REGEX = Regex("\\n|\\\\n")
private val SENTENCE_SPLIT_REGEX = Regex("(?<=(?<!\\.)\\.)(?=\\s+)")
private val HTML_TAG_REGEX = Regex("""<[^>]+>""")
private val MULTI_SPACE_REGEX = Regex("""\s{2,}""")
private val HTML_BLOCK_TAGS_REGEX = Regex("""<(p|li|h1|h2|h3|h4|h5|h6)\b[^<]*(?:(?!<\/\1>)<[^<]*)*<\/\1>""")
private val URL_REGEX = Regex("(http|https)://(www\\.)?[-a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[-a-zA-Z]{2,}(\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)?)")

fun String.separateSentences(): List<String> =
    this.split(NEW_LINE_REGEX)
        .flatMap { it.split(SENTENCE_SPLIT_REGEX) }
        .filter { it.isNotBlank() }

fun String.separateSentencesInsertPTag(): String {
    val text = this.replace("<", "&lt").split("\n")
    return text.joinToString("") { paragraph ->
        if (paragraph.trim().isNotBlank()) {
            paragraph.split(SENTENCE_SPLIT_REGEX)
                .joinToString("") { "<p>${it}</p>" } + "</br>"
        } else {
            "</br>"
        }
    }
}

fun String.separateSentencesInsertPTagWeb(): String =
    this.replace("<", "<</>")
        .split("\n")
        .map { paragraph ->
            paragraph.split(Regex("(?<=\\.)(?=\\s+)")) // Keeping this one local as it's slightly different
                .joinToString("") { "<p>${it}</p>" }
        }
        .filter { it.removeSurrounding("<p>", "</p>").trim().isNotBlank() }
        .joinToString("</br></br>")

fun String.cleanHtmlTags(): String =
    replace(HTML_TAG_REGEX, "")
        .replace(MULTI_SPACE_REGEX, " ").trim()

fun String.translateHTMLtoPlain(): String =
    HTML_BLOCK_TAGS_REGEX.findAll(this)
        .mapNotNull { StringEscapeUtils.unescapeHtml4(it.value.cleanHtmlTags()) }
        .filter { it.trim().isNotBlank() }.joinToString("\n\n")

fun String.translateInnerTextToPlain(): String = this.replace("\\n", "\n").replace("\\u003C", "<")

fun String.isURL(): Boolean = matches(URL_REGEX)
