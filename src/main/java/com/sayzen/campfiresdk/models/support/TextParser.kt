package com.sayzen.campfiresdk.models.support

import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsText

class TextParser(
        val text: String
) {

    private val textLow = text.toLowerCase()
    private var result: String? = null
    private var i = 0
    private var lastCharWasSpace = true
    private var firstWordChar: Char = '.'

    fun parse(): String {
        if (result == null) parseText()
        return result!!
    }

    private fun parseText() {
        result = ""
        while (i < text.length) {
            if (lastCharWasSpace) firstWordChar = text[i]
            lastCharWasSpace = text[i] == ' '

            if (firstWordChar == '@') {
                result += text[i++]
                continue
            }

            if (text[i] == '\\'
                    && text.length > i + 1
                    && (text[i + 1] == '*' || text[i + 1] == '^' || text[i + 1] == '~' || text[i + 1] == '_' || text[i + 1] == '{' || text[i + 1] == '}')) {
                i++
                result += text[i++]
                continue
            }
            if (parse('*', "<\$b>", "</\$b>")) continue
            if (parse('^', "<\$i>", "</\$i>")) continue
            if (parse('~', "<\$s>", "</\$s>")) continue
            if (parse('_', "<\$u>", "</\$u>")) continue
            if (parseLink()) continue
            if (parseColorHash()) continue
            if (parseColorName("red", "D32F2F")) continue
            if (parseColorName("pink", "C2185B")) continue
            if (parseColorName("purple", "7B1FA2")) continue
            if (parseColorName("indigo", "303F9F")) continue
            if (parseColorName("blue", "1976D2")) continue
            if (parseColorName("cyan", "0097A7")) continue
            if (parseColorName("teal", "00796B")) continue
            if (parseColorName("green", "388E3C")) continue
            if (parseColorName("lime", "689F38")) continue
            if (parseColorName("yellow", "FBC02D")) continue
            if (parseColorName("amber", "FFA000")) continue
            if (parseColorName("orange", "F57C00")) continue
            if (parseColorName("brown", "5D4037")) continue
            if (parseColorName("grey", "616161")) continue
            if (parseColorName("campfire", "FF6D00")) continue
            if (parseColorName("rainbow", "-")) continue
            result += text[i++]
        }
    }

    private fun parse(c: Char, open: String, close: String): Boolean {
        if (text[i] == c) {
            val next = findNext(c)
            if (next != -1) {
                result += open + TextParser(text.substring(i + 1, next)).parse() + close
                i = next + 1
                return true
            }
        }
        return false
    }

    private fun findNext(c: Char, offset: Int = 0): Int {
        var next = -1
        for (n in i + 1 + offset until text.length) {
            if (text[n] == c) {
                next = n
                break
            }
        }
        return next
    }

    private fun parseColorName(name: String, hash: String): Boolean {
        try {
            if (text[i] == '{') {
                for (n in 0 until name.length) if (textLow[i + 1 + n] != name[n]) return false

                if (text[i + name.length + 1] == ' ') {
                    val next = findNext('}', name.length + 1)
                    val t = TextParser(text.substring(i + name.length + 2, next)).parse()
                    if (next != -1) {
                        if (name == "rainbow") {
                            var x = -1
                            for (i in t) result += rainbow("$i", x++)
                        } else {
                            result += "<font color=\"#$hash\">$t</font>"
                        }
                        i = next + 1
                        return true
                    }
                }
            }
            return false
        } catch (e: Exception) {
            err(e)
            return false
        }
    }

    private fun rainbow(s: String, index: Int): String {
        return when (index % 7) {
            0 -> "<font color=\"#F44336\">$s</font>"
            1 -> "<font color=\"#FF9800\">$s</font>"
            2 -> "<font color=\"#FFEB3B\">$s</font>"
            3 -> "<font color=\"#4CAF50\">$s</font>"
            4 -> "<font color=\"#2196F3\">$s</font>"
            5 -> "<font color=\"#673AB7\">$s</font>"
            6 -> "<font color=\"#9C27B0\">$s</font>"
            else -> "<font color=\"#9C27B0\">$s</font>"
        }
    }

    private fun parseColorHash(): Boolean {
        try {
            if (text[i] == '{') {
                val c1 = nextColorChar(i + 1)
                val c2 = nextColorChar(i + 2)
                val c3 = nextColorChar(i + 3)
                val c4 = nextColorChar(i + 4)
                val c5 = nextColorChar(i + 5)
                val c6 = nextColorChar(i + 6)
                if (c1 != null && c2 != null && c3 != null && c4 != null && c5 != null && c6 != null && text[i + 7] == ' ') {
                    val color = "" + c1 + c2 + c3 + c4 + c5 + c6
                    val next = findNext('}', 7)
                    if (next != -1) {
                        result += "<font color=\"#$color\">${TextParser(
                                text.substring(
                                        i + 8,
                                        next
                                )
                        ).parse()}</font>"
                        i = next + 1
                        return true
                    }
                }
            }
            return false
        } catch (e: Exception) {
            err(e)
            return false
        }
    }

    private fun parseLink(): Boolean {
        try {
            if (text[i] == '[') {
                val nextClose = findNext(']')

                if (nextClose == -1) return false

                var nextSpace = findNext(' ', nextClose - i)
                if (nextSpace == -1) nextSpace = text.length

                if(ToolsText.TEXT_CHARS_s.contains(text[nextSpace-1])) nextSpace--
                val name = text.substring(i + 1, nextClose)
                val link = text.substring(nextClose + 1, nextSpace)

                if (ToolsText.isWebLink(link)) {
                    result += "<a href=\"${ToolsText.castToWebLink(link)}\">$name</a>"
                    i = nextSpace
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            err(e)
            return false
        }
    }

    private fun nextColorChar(i: Int): Char? {
        if (text[i] == '0' || text[i] == '1' || text[i] == '2' || text[i] == '3' || text[i] == '4' || text[i] == '5' || text[i] == '6' || text[i] == '7' || text[i] == '8' || text[i] == '9' || textLow[i] == 'a' || textLow[i] == 'b' || textLow[i] == 'c' || textLow[i] == 'd' || textLow[i] == 'e' || textLow[i] == 'f') return text[i]
        return null
    }

}