package expert.rightperception.attributesapp.ui.common

import java.util.regex.Pattern

object Utils {
    val colorRegex = Pattern.compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})\$").toRegex()
    val hexRegex = Pattern.compile("^#([a-fA-F0-9]{2})\$").toRegex()
}