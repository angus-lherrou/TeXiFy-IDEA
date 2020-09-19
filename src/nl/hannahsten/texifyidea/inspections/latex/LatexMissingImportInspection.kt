package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSFONTS
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSSYMB
import nl.hannahsten.texifyidea.lang.Package.Companion.DEFAULT
import nl.hannahsten.texifyidea.lang.Package.Companion.MATHTOOLS
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.definitionsAndRedefinitionsInFileSet
import java.util.*

/**
 * Currently works for built-in commands and environments.
 *
 * @author Hannah Schellekens
 */
open class LatexMissingImportInspection : TexifyInspectionBase() {
    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MissingImport"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Missing imports"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {

        if (!TexifySettings.getInstance().automaticDependencyCheck) {
            return emptyList()
        }

        val descriptors = descriptorList()

        val includedPackages = PackageUtils.getIncludedPackages(file)
        analyseCommands(file, includedPackages, descriptors, manager, isOntheFly)
        analyseEnvironments(file, includedPackages, descriptors, manager, isOntheFly)

        return descriptors
    }

    private fun analyseEnvironments(file: PsiFile, includedPackages: Collection<String>,
                                descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager,
                                isOntheFly: Boolean) {
        val environments = file.childrenOfType(LatexEnvironment::class)
        val defined = file.definitionsAndRedefinitionsInFileSet().asSequence()
                .filter { it.isEnvironmentDefinition() }
                .mapNotNull { it.requiredParameter(0) }
                .toSet()

        for (env in environments) {
            // Don't consider environments that have been defined.
            if (env.name()?.text in defined) {
                continue
            }

            val name = env.name()?.text ?: continue
            val environment = DefaultEnvironment[name] ?: continue
            val pack = environment.dependency

            if (pack == DEFAULT || includedPackages.contains(pack.name)) {
                continue
            }

            // amsfonts is included in amssymb
            if (pack == AMSFONTS && includedPackages.contains(AMSSYMB.name)) {
                continue
            }

            // amsmath is included in mathtools
            if (pack == AMSMATH && includedPackages.contains(MATHTOOLS.name)) {
                continue
            }

            descriptors.add(manager.createProblemDescriptor(
                    env,
                    TextRange(7, 7 + name.length),
                    "Environment requires ${pack.name} package",
                    ProblemHighlightType.ERROR,
                    isOntheFly,
                    ImportEnvironmentFix(pack.name)
            ))
        }
    }

    private fun analyseCommands(file: PsiFile, includedPackages: Collection<String>,
                                descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager,
                                isOntheFly: Boolean) {
        val commands = file.commandsInFile()
        commandLoop@ for (command in commands) {
            val name = command.commandToken.text.substring(1)
            val latexCommands = LatexCommand.lookup(name) ?: continue

            // In case there are multiple commands with this name, we don't know which one the user wants.
            // So we don't know which of the dependencies the user needs: we assume that if at least one of them is present it will be the right one.
            val dependencies = latexCommands.map { it.dependency }.toSet()

            // If the command is being defined (e.g. by \newcommand),
            // we don't care that its dependencies aren't included.
            if (command.previousCommand().isCommandDefinition() &&
                    command.previousCommand()?.forcedFirstRequiredParameterAsCommand()?.equals(command) == true) {
                continue
            }

            // TODO (angus-lherrou @ 2020-09-19): stop checking this command once
            //  it's been (re)defined (this would be trivial if the commands in
            //  [PsiFile.commandsInFile] were ordered by position but they're not)

            if (dependencies.isEmpty() || dependencies.any { it.isDefault }) {
                continue
            }

            // Packages included in other packages
            for (packageInclusion in Magic.Package.packagesLoadingOtherPackages) {
                if (packageInclusion.value.intersect(dependencies).isNotEmpty() && includedPackages.contains(packageInclusion.key.name)) {
                    continue@commandLoop
                }
            }

            // If none of the dependencies are included
            if (includedPackages.toSet().intersect(dependencies.map { it.name }).isEmpty()) {
                // We know dependencies is not empty
                val range = TextRange(0, latexCommands.minBy { it.command.length }!!.command.length + 1)
                val dependencyNames = dependencies.joinToString { it.name }.replaceAfterLast(", ", "or ${dependencies.last().name}")
                val fixes = dependencies.map { ImportCommandFix(it) }.toTypedArray()
                descriptors.add(manager.createProblemDescriptor(
                        command,
                        range,
                        "Command requires $dependencyNames package",
                        ProblemHighlightType.ERROR,
                        isOntheFly,
                        *fixes
                ))
            }
        }
    }

    /**
     * @author Hannah Schellekens
     */
    private class ImportCommandFix(val pack: nl.hannahsten.texifyidea.lang.Package) : LocalQuickFix {

        override fun getFamilyName() = "Add import for package '${pack.name}'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile

            if (!PackageUtils.insertUsepackage(file, pack)) {
                Notification("LatexMissingImportInspection", "Conflicting package detected", "The package ${pack.name} was not inserted because a conflicting package was detected.", NotificationType.INFORMATION).notify(project)
            }
        }
    }

    /**
     * @author Hannah Schellekens
     */
    private class ImportEnvironmentFix(val import: String) : LocalQuickFix {

        override fun getFamilyName() = "Add import for package '$import'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val environment = descriptor.psiElement as? LatexEnvironment ?: return
            val thingy = DefaultEnvironment.fromPsi(environment) ?: return
            val file = environment.containingFile

            PackageUtils.insertUsepackage(file, thingy.dependency)
        }
    }
}