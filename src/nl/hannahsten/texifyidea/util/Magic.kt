package nl.hannahsten.texifyidea.util

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.file.*
import nl.hannahsten.texifyidea.inspections.latex.LatexLineBreakInspection
import nl.hannahsten.texifyidea.lang.CommandManager
import nl.hannahsten.texifyidea.lang.LatexRegularCommand.*
import nl.hannahsten.texifyidea.lang.Package
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSFONTS
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSSYMB
import nl.hannahsten.texifyidea.lang.Package.Companion.BIBLATEX
import nl.hannahsten.texifyidea.lang.Package.Companion.GLOSSARIES
import nl.hannahsten.texifyidea.lang.Package.Companion.GLOSSARIESEXTRA
import nl.hannahsten.texifyidea.lang.Package.Companion.GRAPHICS
import nl.hannahsten.texifyidea.lang.Package.Companion.GRAPHICX
import nl.hannahsten.texifyidea.lang.Package.Companion.MATHTOOLS
import nl.hannahsten.texifyidea.lang.Package.Companion.NATBIB
import nl.hannahsten.texifyidea.lang.Package.Companion.XCOLOR
import org.intellij.lang.annotations.Language
import java.awt.Color
import java.util.regex.Pattern

typealias LatexPackage = Package
typealias RegexPattern = Pattern

/**
 * Magic constants are awesome!
 *
 * @author Hannah Schellekens
 */
object Magic {

    /**
     * @author Hannah Schellekens
     */
    object General {

        @JvmField
        val noQuickFix: LocalQuickFix? = null

        /**
         * Abbreviations not detected by [Pattern.abbreviation].
         */
        val unRegexableAbbreviations = listOf(
            "et al."
        )

        @JvmField
        val latexDemoText = """
                |%
                |% An amazing example for LaTeX.
                |%
                |\documentclass[<optionalParam>12pt,a4paper</optionalParam>]{article}
                |
                |% Package imports.
                |\usepackage{amsmath}
                |\usepackage{listings}
                |
                |% Document wide TikZ settings.
                |\tikzset{
                |   mystyle/.style={
                |        draw,
                |        circle,
                |        label={[fill=yellow]0:#1}
                |    }
                |}
                |
                |% Very incomplete Kotlin definition for listings.
                |\lstdefinelanguage{Kotlin}{
                |basicstyle={\ttfamily},
                |    keywords={fun, if, else, this, let},
                |    keywordstyle={\color{orange!80!red}},
                |}
                |
                |% Define title.
                |\title{A Very Simple \LaTeXe{} Template}
                |\author{
                |        Henk-Jan\\Department of YUROP\\University of Cheese\\
                |        Windmill City, 2198 AL, \underline{Tulipa}
                |}
                |\date{\today}
                |
                |% Start document.
                |\begin{document}
                |    \maketitle
                |
                |    % Start writing amazing stuff now.
                |    \begin{abstract}
                |        This is the paper's abstract.
                |        In this paper, we do basically nothing.
                |    \end{abstract}
                |
                |    \section{Introduction}\label{sec:introduction}
                |    This is time for all good women to come to the aid of their party!
                |
                |    \section{Mathematics}\label{sec:mathematics}
                |    To start the party, as ``announced'' in Section~\ref{sec:introduction}, please take a look at the value of <inlineMath>${'$'}x <inlineCommand>\times</inlineCommand>
                |    <inlineCommand>\frac</inlineCommand>{5}{<inlineCommand>\sqrt</inlineCommand>{3}}${'$'}</inlineMath> in the following equation:
                |    <displayMath>\[
                |       x <displayCommand>\times</displayCommand> <displayCommand>\frac</displayCommand>{5}{<displayCommand>\sqrt</displayCommand>{3}} = y <displayCommand>\cdot</displayCommand> <displayCommand>\max\left</displayCommand>{ 4, <displayCommand>\alpha</displayCommand>, 6 <displayCommand>\right</displayCommand>} +
                |           <displayCommand>\sqrt</displayCommand>[<optionalParam>1234</optionalParam>]{5678}.
                |    \]</displayMath>
                |    
                |    \paragraph{Programming}
                |    % @formatter:off
                |    \begin{lstlisting}[language=Kotlin]
                |fun Int?.ifPositiveAddTwo(): Int =
                |        this?.let {
                |            if (this >= 0) this + 2
                |            else this
                |        } ?: 0
                |    \end{lstlisting}
                |    % @formatter:on
                |
                |    \subsection{More work}\label{subsec:moreWork}
                |    A much longer \LaTeXe{} example was written by Henk-Jan~\cite{Gil:02}.
                |    But we can also just do some more epic plugin showoffy stuff like
                |    <displayMath>\begin{align}
                |       <displayCommand>\text</displayCommand>{Stuff here is also highlighted, and also }
                |       <displayCommand>\sum</displayCommand>_{i=0}^n <displayCommand>\left</displayCommand>( i <displayCommand>\right</displayCommand>)
                |    \end{align}</displayMath>
                |
                |    \section{Results}\label{sec:results}
                |    In this section we describe the results. 
                |    So basically <inlineMath>${'$'}x${'$'}</inlineMath> but maybe also <inlineMath>${'$'}<inlineCommand>\hat</inlineCommand>{x}^{2y}${'$'}</inlineMath>.
                |
                |    \section{Conclusions}\label{sec:conclusions}
                |    We worked hard, and achieved very little. 
                |    Or did we?
                |
                |    % Another extremely descriptive comment.
                |    \bibliographystyle{abbrv}
                |    \bibliography{main}
                |
                |\end{document}
        """.trimMargin()

        @Language("Bibtex")
        @JvmField
        val bibtexDemoText = """
                % I am a BibTeX comment.
                @article{greenwade1993,
                    author  = "George D. Greenwade",
                    title   = "The {C}omprehensive {T}ex {A}rchive {N}etwork ({CTAN})",
                    year    = "1993",
                    journal = "TUGBoat",
                    volume  = "14",
                    number  = "3",
                    pages   = "342--351"
                }
                
                I am also a BibTeX comment.
                @book{goossens1993,
                    author    = "Michel Goossens and Frank Mittelbach and Alexander Samarin",
                    title     = "The LaTeX Companion",
                    year      = "1993",
                    publisher = "Addison-Wesley",
                    address   = "Reading, Massachusetts"
                }
        """.trimIndent()
    }

    /**
     * @author Hannah Schellekens
     */
    object Typography {

        /**
         * Matches each (supported) opening brace to it's opposite close brace.
         */
        @JvmField
        val braceOpposites = mapOfVarargs(
                "(", ")",
                "[", "]",
                "\\{", "\\}",
                "<", ">",
                "|", "|",
                "\\|", "\\|"
        )

        /**
         * Algorithmicx pairs (also hardcoded in lexer).
         */
        val pseudoCodeBeginEndOpposites = mapOf(
                "If" to "EndIf",
                "For" to "EndFor",
                "ForAll" to "EndFor",
                "While" to "EndWhile",
                "Repeat" to "Until",
                "Loop" to "EndLoop",
                "Function" to "EndFunction",
                "Procedure" to "EndProcedure"
        )
    }

    /**
     * @author Hannah Schellekens
     */
    object Environment {

        @JvmField
        val listingEnvironments = hashSetOf("itemize", "enumerate", "description")

        @JvmField
        val tableEnvironments = hashSetOf("tabular", "tabular*", "tabularx", "array", "longtable")

        /**
         * Map that maps all environments that are expected to have a label to the label prefix they have by convention.
         *
         * environment name `=>` label prefix without colon
         */
        @JvmField
        val labeled = mapOfVarargs(
                "figure", "fig",
                "table", "tab",
                "equation", "eq",
                "algorithm", "alg",
                "lstlisting", "lst",
                "Verbatim", "verb"
        )

        /**
         * Environments that define their label via an optional parameter
         */
        @JvmField
        val labelAsParameter = hashSetOf("lstlisting", "Verbatim")

        /**
         * Environments that introduce figures
         */
        @JvmField
        val figures = hashSetOf("figure")

        @JvmField
        val verbatim = hashSetOf("verbatim", "Verbatim", "lstlisting", "plantuml", "luacode", "luacode*")

        @JvmField
        val algorithmEnvironments = setOf("algorithmic")

        /**
         * All environments that define a matrix.
         */
        @JvmField
        val matrixEnvironments = setOf(
                "matrix", "pmatrix", "bmatrix", "vmatrix", "Bmatrix", "Vmatrix",
                "matrix*", "pmatrix*", "bmatrix*", "vmatrix*", "Bmatrix*", "Vmatrix*",
                "smallmatrix", "psmallmatrix", "bsmallmatrix", "vsmallmatrix", "Bsmallmatrix", "Vsmallmatrix",
                "smallmatrix*", "psmallmatrix*", "bsmallmatrix*", "vsmallmatrix*", "Bsmallmatrix*", "Vsmallmatrix*",
                "gmatrix"
        )

        @JvmField
        val alignableEnvironments = setOf(
                "eqnarray", "eqnarray*",
                "split",
                "align", "align*",
                "alignat", "alignat*",
                "flalign", "flalign*",
                "aligned", "alignedat",
                "cases", "dcases"
        ) + matrixEnvironments
    }

    /**
     * @author Abby Berkers
     */
    object Comment {

        @JvmField
        val preambleValues = hashSetOf("tikz", "math")
    }

    /**
     * @author Hannah Schellekens
     */
    object Command {

        /**
         * LaTeX commands that make the text take up more vertical space.
         */
        @JvmField
        val high = hashSetOf(
                "\\frac", "\\dfrac", "\\sqrt", "\\sum", "\\int", "\\iint", "\\iiint", "\\iiiint",
                "\\prod", "\\bigcup", "\\bigcap", "\\bigsqcup", "\\bigsqcap"
        )

        /**
         * Maps commands to their expected label prefix. Which commands are expected to have a label at all is determined in settings.
         */
        @JvmField
        val labeledPrefixes = mapOf(
                "\\" + CHAPTER.command to "ch",
                "\\" + SECTION.command to "sec",
                "\\" + SUBSECTION.command to "subsec",
                "\\" + SUBSUBSECTION.command to "subsubsec",
                "\\" + ITEM.command to "itm"
        )

        /**
         * Level of labeled commands.
         */
        val labeledLevels = mapOf(
            // See page 23 of the LaTeX Companion
            PART to -1, // actually, it is level 0 in classes that do not define \chapter and -1 in book and report
            CHAPTER to 0,
            SECTION to 1,
            SUBSECTION to 2,
            SUBSUBSECTION to 3,
            PARAGRAPH to 4,
            SUBPARAGRAPH to 5
        )

        /**
         * All commands that mark some kind of section.
         */
        @JvmField
        val sectionMarkers = listOf(
            PART, CHAPTER, SECTION, SUBSECTION, SUBSUBSECTION, PARAGRAPH, SUBPARAGRAPH
        ).map { "\\" + it.command }

        /**
         * The colours that each section separator has.
         */
        @JvmField
        val sectionSeparatorColors = mapOf(
            "\\${PART.command}" to Color(152, 152, 152),
            "\\${CHAPTER.command}" to Color(172, 172, 172),
            "\\${SECTION.command}" to Color(182, 182, 182),
            "\\${SUBSECTION.command}" to Color(202, 202, 202),
            "\\${SUBSUBSECTION.command}" to Color(212, 212, 212),
            "\\${PARAGRAPH.command}" to Color(222, 222, 222),
            "\\${SUBPARAGRAPH.command}" to Color(232, 232, 232)
        )

        /**
         * LaTeX commands that increase a counter that can be labeled.
         */
        @JvmField
        val increasesCounter = hashSetOf("\\caption", "\\captionof") + labeledPrefixes.keys

        /**
         * All commands that represent a reference to a label, excluding user defined commands.
         */
        @JvmField
        val labelReferenceWithoutCustomCommands = hashSetOf(
                "\\ref", "\\eqref", "\\nameref", "\\autoref",
                "\\fullref", "\\pageref", "\\vref", "\\Autoref", "\\cref", "\\Cref",
                "\\labelcref", "\\cpageref"
        )

        /**
         * All commands that represent a reference to a label, including user defined commands.
         */
        fun getLabelReferenceCommands(project: Project): Set<String> {
            CommandManager.updateAliases(labelReferenceWithoutCustomCommands, project)
            return CommandManager.getAliases(labelReferenceWithoutCustomCommands.first())
        }

        /**
         * All commands that represent a reference to a bibliography entry/item.
         * Commands listed here should also be listed in [nl.hannahsten.texifyidea.lang.LatexRegularCommand].
         */
        @JvmField
        val bibliographyReference = hashSetOf(
                "\\cite", "\\nocite", "\\citep", "\\citep*", "\\citet", "\\citet*", "\\Citep",
                "\\Citep*", "\\Citet", "\\Citet*", "\\citealp", "\\citealp*", "\\citealt", "\\citealt*",
                "\\Citealp", "\\Citealp*", "\\Citealt", "\\Citealt*", "\\citeauthor", "\\citeauthor*",
                "\\Citeauthor", "\\Citeauthor*", "\\citeyear", "\\citeyearpar", "\\parencite", "\\Parencite",
                "\\footcite", "\\footcitetext", "\\textcite", "\\Textcite", "\\smartcite", "\\Smartcite",
                "\\cite*", "\\parencite*", "\\supercite", "\\autocite", "\\Autocite", "\\autocite*",
                "\\Autocite*", "\\citetitle", "\\citetitle*", "\\citeyear*", "\\citedate", "\\citedate*",
                "\\citeurl", "\\volcite", "\\Volcite", "\\pvolcite", "\\Pvolcite", "\\fvolcite",
                "\\Fvolcite", "\\ftvolcite", "\\svolcite", "\\Svolcite", "\\tvolcite", "\\Tvolcite",
                "\\avolcite", "\\Avolcite", "\\fullcite", "\\footfullcite", "\\notecite", "\\Notecite",
                "\\pnotecite", "\\fnotecite"
        )

        /**
         * All commands that represent some kind of reference (think \ref and \cite).
         */
        @JvmField
        val reference = labelReferenceWithoutCustomCommands + bibliographyReference

        /**
         * Commands from the import package which require an absolute path as first parameter.
         */
        val absoluteImportCommands = setOf("\\includefrom", "\\inputfrom", "\\import")

        /**
         * Commands from the import package which require a relative path as first parameter.
         */
        val relativeImportCommands = setOf("\\subimport", "\\subinputfrom", "\\subincludefrom")

        /**
         * All commands for which we assume that commas in required parameters do not separate parameters.
         * By default we assume the comma is a separator.
         */
        val commandsWithNoCommaSeparatedParameters = setOf(INCLUDEGRAPHICS).map { "\\" + it.command }

        /**
         * All commands that define labels and that are present by default.
         * To include user defined commands, use [getLabelDefinitionCommands] (may be significantly slower).
         */
        @JvmField
        val labelDefinitionsWithoutCustomCommands = setOf("\\label")

        /**
         * Get all commands defining labels, including user defined commands.
         * If you need to know which parameters of user defined commands define a label, use [CommandManager.labelAliasesInfo].
         *
         * This will check if the cache of user defined commands needs to be updated, based on the given project, and therefore may take some time.
         */
        fun getLabelDefinitionCommands(project: Project): Set<String> {
            // Check if updates are needed
            CommandManager.updateAliases(labelDefinitionsWithoutCustomCommands, project)
            return CommandManager.getAliases(labelDefinitionsWithoutCustomCommands.first())
        }

        /**
         * Get all commands defining labels, including user defined commands. This will not check if the aliases need to be updated.
         */
        fun getLabelDefinitionCommands() = CommandManager.getAliases(labelDefinitionsWithoutCustomCommands.first())

        /**
         * All commands that define bibliography items.
         */
        @JvmField
        val bibliographyItems = setOf("\\" + BIBITEM.command)

        /**
         * All math operators without a leading slash.
         */
        @JvmField
        val slashlessMathOperators = hashSetOf(
                "arccos", "arcsin", "arctan", "arg", "cos", "cosh", "cot", "coth", "csc",
                "deg", "det", "dim", "exp", "gcd", "hom", "inf", "ker", "lg", "lim", "liminf", "limsup",
                "ln", "log", "max", "min", "Pr", "sec", "sin", "sinh", "sup", "tan", "tanh"
        )

        /**
         * All commands that define regular commands, and that require that the command is not already defined.
         */
        val regularStrictCommandDefinitions = hashSetOf(
                "\\" + NEWCOMMAND.command,
                "\\" + NEWCOMMAND_STAR.command,
                "\\" + NEWIF.command,
                "\\" + NEWDOCUMENTCOMMAND.command
        )

        /**
         * All commands that define or redefine other commands, whether it exists or not.
         */
        @JvmField
        val redefinitions = hashSetOf(
                RENEWCOMMAND,
                RENEWCOMMAND_STAR,
                PROVIDECOMMAND, // Does nothing if command exists
                PROVIDECOMMAND_STAR,
                PROVIDEDOCUMENTCOMMAND, // Does nothing if command exists
                DECLAREDOCUMENTCOMMAND,
                DEF,
                LET,
                RENEWENVIRONMENT
        ).map { "\\" + it.command }

        /**
         * All commands that define or redefine regular commands.
         */
        val regularCommandDefinitions = regularStrictCommandDefinitions + redefinitions

        /**
         * All commands that define commands that should be used exclusively
         * in math mode.
         */
        @JvmField
        val mathCommandDefinitions = hashSetOf(
                "\\" + DECLARE_MATH_OPERATOR.command,
                "\\" + DECLARE_PAIRED_DELIMITER.command,
                "\\" + DECLARE_PAIRED_DELIMITER_X.command,
                "\\" + DECLARE_PAIRED_DELIMITER_XPP.command
        )

        /**
         * All commands that define new commands.
         */
        @JvmField
        val commandDefinitions = regularCommandDefinitions + mathCommandDefinitions

        /**
         * All commands that define new documentclasses.
         */
        @JvmField
        val classDefinitions = hashSetOf("\\" + PROVIDESCLASS.command)

        /**
         * All commands that define new packages.
         */
        @JvmField
        val packageDefinitions = hashSetOf("\\" + PROVIDESPACKAGE.command)

        /**
         * All commands that define new environments.
         */
        @JvmField
        val environmentDefinitions = hashSetOf(
            NEWENVIRONMENT,
            NEWTHEOREM,
            NEWDOCUMENTENVIRONMENT,
            PROVIDEDOCUMENTENVIRONMENT,
            DECLAREDOCUMENTENVIRONMENT
        ).map { "\\" + it.command }

        /**
         * All commands that define stuff like classes, environments, and definitions.
         */
        @JvmField
        val definitions = commandDefinitions + classDefinitions + packageDefinitions + environmentDefinitions

        /**
         * Commands for which TeXiFy-IDEA has essential custom behaviour and which should not be redefined.
         */
        @JvmField
        val fragile = hashSetOf(
                "\\addtocounter", "\\begin", "\\chapter", "\\def", "\\documentclass", "\\end",
                "\\include", "\\includeonly", "\\input", "\\label", "\\let", "\\newcommand",
                "\\overline", "\\paragraph", "\\part", "\\renewcommand", "\\section", "\\setcounter",
                "\\sout", "\\subparagraph", "\\subsection", "\\subsubsection", "\\textbf",
                "\\textit", "\\textsc", "\\textsl", "\\texttt", "\\underline", "\\[", "\\]",
                "\\newenvironment", "\\bibitem",
                "\\NewDocumentCommand",
                "\\ProvideDocumentCommand",
                "\\DeclareDocumentCommand",
                "\\NewDocumentEnvironment",
                "\\ProvideDocumentEnvironment",
                "\\DeclareDocumentEnvironment"
        )

        /**
         * Commands that should not have the given file extensions.
         */
        @JvmField
        val illegalExtensions = mapOf(
                "\\" + INCLUDE.command to listOf(".tex"),
                "\\" + SUBFILEINCLUDE.command to listOf(".tex"),
                "\\" + BIBLIOGRAPHY.command to listOf(".bib")
        )

        /**
         * Commands that should have the given file extensions.
         */
        @JvmField
        val requiredExtensions = mapOf(
                "\\" + ADDBIBRESOURCE.command to listOf("bib")
        )

        /**
         * Extensions that should only be scanned for the provided include commands.
         */
        @JvmField
        val includeOnlyExtensions: Map<String, HashSet<String>> = mapOf(
                "\\include" to hashSetOf("tex"),
                "\\includeonly" to hashSetOf("tex"),
                "\\subfile" to hashSetOf("tex"),
                "\\subfileinclude" to hashSetOf("tex"),
                "\\bibliography" to hashSetOf("bib"),
                "\\addbibresource" to hashSetOf("bib"),
                "\\RequirePackage" to hashSetOf("sty"),
                "\\usepackage" to hashSetOf("sty"),
                "\\documentclass" to hashSetOf("cls"),
                "\\" + EXTERNALDOCUMENT.command to hashSetOf("tex") // Not completely true, as it only includes labels
        )

        val includeCommands = includeOnlyExtensions.keys

        /**
         * All commands that end if.
         */
        @JvmField
        val endIfs = hashSetOf("\\fi")

        /**
         * All commands that at first glance look like \if-esque commands, but that actually aren't.
         */
        @JvmField
        val ignoredIfs = hashSetOf("\\newif", "\\iff", "\\ifthenelse", "\\iftoggle", "\\ifoot")

        /**
         * List of all TeX style primitives.
         */
        @JvmField
        val stylePrimitives = listOf(
                "\\rm", "\\sf", "\\tt", "\\it", "\\sl", "\\sc", "\\bf"
        )

        /**
         * The LaTeX counterparts of all [stylePrimitives] commands where %s is the content.
         */
        @JvmField
        val stylePrimitveReplacements = listOf(
                "\\textrm{%s}", "\\textsf{%s}", "\\texttt{%s}", "\\textit{%s}",
                "\\textsl{%s}", "\\textsc{%s}", "\\textbf{%s}"
        )

        /**
         * Set of text styling commands
         */
        @JvmField
        val textStyles = setOf(
                "\\textrm", "\\textsf", "\\texttt", "\\textit",
                "\\textsl", "\\textsc", "\\textbf", "\\emph",
                "\\textup", "\\textmd"
        )

        /**
         * All LaTeX commands that contain a url (in their first parameter).
         */
        @JvmField
        val urls = hashSetOf("\\url", "\\href")

        /**
         * All BibTeX tags that take a url as their parameter.
         */
        @JvmField
        val bibUrls = hashSetOf("url", "biburl")
    }

    /**
     * @author Hannah Schellekens
     */
    object Pattern {

        @JvmField
        val ellipsis = Regex("""(?<!\.)(\.\.\.)(?!\.)""")

        /**
         * This is the only correct way of using en dashes.
         */
        @JvmField
        val correctEnDash = RegexPattern.compile("[0-9]+--[0-9]+")!!

        /**
         * Matches the prefix of a label. Amazing comment this is right?
         */
        @JvmField
        val labelPrefix = RegexPattern.compile(".*:")!!

        /**
         * Matches the end of a sentence.
         *
         * Includes `[^.][^.]` because of abbreviations (at least in Dutch) like `s.v.p.`
         */
        @JvmField
        val sentenceEnd = RegexPattern.compile("([^.A-Z][^.A-Z][.?!;;] +[^%\\s])|(^\\. )")!!

        /**
         * Matches all interpunction that marks the end of a sentence.
         */
        @JvmField
        val sentenceSeparator = RegexPattern.compile("[.?!;;]")!!

        /**
         * Matches all sentenceSeparators at the end of a line (with or without space).
         */
        @JvmField
        val sentenceSeparatorAtLineEnd = RegexPattern.compile("$sentenceSeparator\\s*$")!!

        /**
         * Matches when a string ends with a non breaking space.
         */
        @JvmField
        val endsWithNonBreakingSpace = RegexPattern.compile("~$")!!

        /**
         * Finds all abbreviations that have at least two letters separated by comma's.
         *
         * It might be more parts, like `b.v.b.d. ` is a valid abbreviation. Likewise are `sajdflkj.asdkfj.asdf ` and
         * `i.e. `. Single period abbreviations are not being detected as they can easily be confused with two letter words
         * at the end of the sentence (also localisation...) For this there is a quickfix in [LatexLineBreakInspection].
         */
        @JvmField
        val abbreviation = RegexPattern.compile("[0-9A-Za-z.]+\\.[A-Za-z](\\.[\\s~])")!!

        /** [abbreviation]s that are missing a normal space (or a non-breaking space) */
        val abbreviationWithoutNormalSpace = RegexPattern.compile("[0-9A-Za-z.]+\\.[A-Za-z](\\.[\\s])")!!

        /**
         * Matches all comments, starting with % and ending with a newline.
         */
        val comments: RegexPattern = RegexPattern.compile("%(.*)\\n")

        /**
         * Matches everything except comments which start with % and end with a newline.
         */
        @JvmField
        val noComments: RegexPattern = RegexPattern.compile("(?<=^|\\n)[^%]+")

        /**
         * Matches leading and trailing whitespace on a string.
         */
        @JvmField
        val excessWhitespace = RegexPattern.compile("(^(\\s+).*(\\s*)\$)|(^(\\s*).*(\\s+)\$)")!!

        /**
         * Matches a non-ASCII character.
         */
        @JvmField
        val nonAscii = RegexPattern.compile("\\P{ASCII}")!!

        /**
         * Separator for multiple parameter values in one parameter.
         *
         * E.g. when you have \cite{citation1,citation2,citation3}, this pattern will match the separating
         * comma.
         */
        @JvmField
        val parameterSplit = RegexPattern.compile("\\s*,\\s*")!!

        /**
         * Matches the extension of a file name.
         */
        @JvmField
        val fileExtension = RegexPattern.compile("\\.[a-zA-Z0-9]+$")!!

        /**
         * Matches all leading whitespace.
         */
        @JvmField
        val leadingWhitespace = RegexPattern.compile("^\\s*")!!

        /**
         * Matches newlines.
         */
        @JvmField
        val newline = RegexPattern.compile("\\n")!!

        /**
         * Checks if the string is `text`, two newlines, `text`.
         */
        @JvmField
        val containsMultipleNewlines = RegexPattern.compile("[^\\n]*\\n\\n+[^\\n]*")!!

        /**
         * Matches HTML tags of the form `<tag>`, `<tag/>` and `</tag>`.
         */
        @JvmField
        val htmlTag = RegexPattern.compile("""<.*?/?>""")!!

        /**
         * Matches a LaTeX command that is the start of an \if-\fi structure.
         */
        @JvmField
        val ifCommand = RegexPattern.compile("\\\\if[a-zA-Z@]*")!!

        /**
         * Matches the begin and end commands of the cases and split environments.
         */
        @JvmField
        val casesOrSplitCommands = Regex("((?=\\\\begin\\{cases})|(?<=\\\\begin\\{cases}))" +
                "|((?=\\\\end\\{cases})|(?<=\\\\end\\{cases}))" +
                "|((?=\\\\begin\\{split})|(?<=\\\\begin\\{split}))" +
                "|((?=\\\\end\\{split})|(?<=\\\\end\\{split}))")
    }

    /**
     * @author Hannah Schellekens
     */
    object File {

        /**
         * All file extensions of files that can be included (and where the included files contain language that needs to be considered).
         */
        @JvmField
        val includeExtensions = hashSetOf("tex", "sty", "cls", "bib")

        val automaticExtensions = mapOf(
                "\\include" to LatexFileType.defaultExtension,
                "\\bibliography" to BibtexFileType.defaultExtension
        )

        /**
         * All possible file types.
         */
        @JvmField
        val fileTypes = setOf(
                LatexFileType,
                StyleFileType,
                ClassFileType,
                BibtexFileType,
                TikzFileType
        )

        /**
         * All file extensions that have to be deleted when clearing auxiliary files.
         *
         * This list is the union of @generated_exts in latexmk and the defined Auxiliary files in TeXWorks, plus some additions.
         * (https://github.com/TeXworks/texworks/blob/9c8cc8b88505103cb8f43fe4105638c77c7e7303/res/resfiles/configuration/texworks-config.txt#L37).
         */
        @JvmField
        val auxiliaryFileTypes = arrayOf("aux", "bbl", "bcf", "brf", "fls", "idx", "ind", "lof", "lot", "nav", "out", "snm", "toc", "glo", "gls", "ist", "xdy")

        /**
         * All file extensions that are probably generated by LaTeX and some common packages.
         * Because any package can generated new files, this list is not complete.
         */
        // Actually run.xml should be included (latexmk) but file extensions with a dot are not found currently, see Utils#File.extension
        val generatedFileTypes = auxiliaryFileTypes + arrayOf("blg", "dvi", "fdb_latexmk", "ilg", "log", "out.ps", "pdf", "xml", "sagetex.sage", "sagetex.scmd", "sagetex.sout", "synctex", "gz", "synctex(busy)", "upa", "doctest.sage", "xdv", "glg", "glstex")

        /**
         * All bibtex keys which have a reference to a (local) file in the content.
         */
        val bibtexFileKeys = setOf("bibsource", "file")

        /**
         * Extensions of index-related files, which are generated by makeindex-like programs and should be copied next to the main file for the index/glossary packages to work.
         */
        val indexFileExtensions = setOf("ind", "glo", "ist", "xdy")

        /**
         * Extensions of files required by bib2gls
         */
        val bib2glsDependenciesExtensions = setOf("aux", "glg", "log")
    }

    /**
     * @author Hannah Schellekens
     */
    object Package {

        /**
         * All unicode enabling packages.
         */
        @JvmField
        val unicode = hashSetOf(
                LatexPackage.INPUTENC.with("utf8"),
                LatexPackage.FONTENC.with("T1")
        )

        /**
         * All known packages which provide an index.
         */
        val index = hashSetOf(
                "makeidx", "multind", "index", "splitidx", "splitindex", "imakeidx", "hvindex", "idxlayout", "repeatindex", "indextools"
        )

        /**
         * Packages which provide a glossary.
         */
        val glossary = hashSetOf(GLOSSARIES, GLOSSARIESEXTRA).map { it.name }

        /**
         * Known conflicting packages.
         */
        val conflictingPackages = listOf(
                setOf(BIBLATEX, NATBIB)
        )

        /**
         * Maps packages to the packages it loads.
         */
        val packagesLoadingOtherPackages = mapOf(
                AMSSYMB to setOf(AMSFONTS),
                MATHTOOLS to setOf(AMSMATH),
                GRAPHICX to setOf(GRAPHICS),
                XCOLOR to setOf(COLOR)
        )

        /**
         * Maps argument specifiers to whether they are required (true) or
         * optional (false).
         */
        val xparseParamSpecifiers = mapOf(
                'm' to true,
                'r' to true,
                'R' to true,
                'v' to true,
                'b' to true,
                'o' to false,
                'd' to false,
                'O' to false,
                'D' to false,
                's' to false,
                't' to false,
                'e' to false,
                'E' to false
        )
    }

    /**
     * @author Hannah Schellekens
     */
    object Icon {

        /**
         * Maps file extentions to their corresponding icons.
         */
        @JvmField
        val fileIcons = mapOf(
                "pdf" to TexifyIcons.PDF_FILE,
                "dvi" to TexifyIcons.DVI_FILE,
                "synctex.gz" to TexifyIcons.SYNCTEX_FILE,
                "bbl" to TexifyIcons.BBL_FILE,
                "aux" to TexifyIcons.AUX_FILE,
                "tmp" to TexifyIcons.TEMP_FILE,
                "dtx" to TexifyIcons.DOCUMENTED_LATEX_SOURCE,
                "bib" to TexifyIcons.BIBLIOGRAPHY_FILE,
                "toc" to TexifyIcons.TABLE_OF_CONTENTS_FILE,
                "tikz" to TexifyIcons.TIKZ_FILE
        )
    }

    object Colors {
        /**
         * All commands that have a color as an argument.
         */
        @JvmField
        val takeColorCommands = values()
                .filter {
                    it.arguments.map { it.name }.contains("color")
                }
                .map { it.command }

        /**
         * All commands that define a new color.
         */
        @JvmField
        val colorDefinitions = values()
                .filter { it.dependency == XCOLOR }
                .filter { it.arguments.map { it.name }.contains("name") }

        @JvmField
        val colorCommands = takeColorCommands + colorDefinitions.map { it.command }

        @JvmField
        val defaultXcolors = mapOf(
                "red" to 0xff0000,
                "green" to 0x00ff00,
                "blue" to 0x0000ff,
                "cyan" to 0x00ffff,
                "magenta" to 0xff00ff,
                "yellow" to 0xffff00,
                "black" to 0x000000,
                "gray" to 0x808080,
                "white" to 0xffffff,
                "darkgray" to 0x404040,
                "lightgray" to 0xbfbfbf,
                "brown" to 0xfb8040,
                "lime" to 0xbfff00,
                "olive" to 0x808000,
                "orange" to 0xff8000,
                "pink" to 0xffbfbf,
                "purple" to 0xbf0040,
                "teal" to 0x008080,
                "violet" to 0x800080
        )
    }
}