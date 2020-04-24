package nl.hannahsten.texifyidea.run.latex.logtab.messagehandlers.warnings

import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.run.latex.logtab.LatexMessageHandler
import nl.hannahsten.texifyidea.run.latex.logtab.LogMagicRegex

object LatexLineWarningHandler : LatexMessageHandler(
        WARNING,
        """${LogMagicRegex.LATEX_WARNING_REGEX}(?<message>.+)${LogMagicRegex.LINE_REGEX}""".toRegex()
) {
    override fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        LatexLineWarningHandler.regex.forEach {
            it.find(text)?.apply {
                val message = groups["message"]?.value?.trim() ?: return@apply
                val line = groups["line"]?.value?.toInt() ?: return@apply
                return LatexLogMessage(message.replace("(Font)", ""), fileName = currentFile, type = super.messageType, line = line)
            }
        }
        return null
    }
}