package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.util.files.FileUtil
import nl.hannahsten.texifyidea.util.files.createExcludedDir
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import java.io.File

/**
 * Output file as a virtual file, or a promise to provide a path that can be constructed when the run configuration is actually created.
 * This allows for custom output paths in the run configuration template.
 *
 * Supported placeholders:
 * - $contentRoot
 * - $mainFile
 *
 * @param variant: out or auxil
 */
// todo is mainFile updated? Or pass to every method?
class LatexOutputPath(private val variant: String, private val contentRoot: VirtualFile?, private val mainFile: VirtualFile?, private val project: Project) {
    private val projectDirString = "${'$'}projectDir"
    private val mainFileString = "${'$'}mainFile"

    var virtualFile: VirtualFile? = null
    var pathString: String = "$projectDirString/$variant"

    fun getPath(): VirtualFile {
        // When the user modifies the run configuration template, then this variable will magically be replaced with the
        // path to the /bin folder of IntelliJ, without the setter being called.
        if (virtualFile?.path?.endsWith("/bin") == true) {
            virtualFile = null
        }

        if (virtualFile != null) {
            return virtualFile!!
        }
        else {
            val pathString = if (pathString.contains(projectDirString)) {
                pathString.replace(projectDirString, contentRoot?.path ?: "")
            }
            else {
                pathString.replace(mainFileString, mainFile?.path ?: "")
            }
            val path = LocalFileSystem.getInstance().findFileByPath(pathString)
            // todo create path if not exists
            // todo check path is directory
            if (path != null) {
                return path
            }
            // Path is invalid (perhaps the user provided an invalid path
            // todo create and return default path
            return contentRoot!!
        }
    }

    /**
     * Assuming the main file is known, set a default output path if not already set.
     */
    fun setDefault() {
        if (virtualFile != null || mainFile == null) return
        this.virtualFile = getDefaultOutputPath()
    }

    private fun getDefaultOutputPath(): VirtualFile? {
        if (mainFile == null) return null
        var defaultOutputPath: VirtualFile? = null
        runReadAction {
            val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile)
            defaultOutputPath = LocalFileSystem.getInstance().findFileByPath(moduleRoot?.path + "/" + variant)
        }
        return defaultOutputPath
    }

    /**
     * Whether the current output path is the default.
     */
    fun isDefault() = getDefaultOutputPath() == virtualFile


    /**
     * Creates the output directories to place all produced files.
     */
    fun create() {
        val mainFile = mainFile ?: return

        val fileIndex = ProjectRootManager.getInstance(project).fileIndex

        val includeRoot = mainFile.parent
        val parentPath = fileIndex.getContentRootForFile(mainFile, false)?.path ?: includeRoot.path
        val outPath = "$parentPath/out"

        // Create output path for non-MiKTeX systems (MiKTeX creates it automatically)
        val module = fileIndex.getModuleForFile(mainFile, false)
        File(outPath).mkdirs()
        virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(outPath)
        module?.createExcludedDir(outPath)
    }

    /**
     * Copy subdirectories of the source directory to the output directory for includes to work in non-MiKTeX systems
     */
    @Throws(ExecutionException::class)
    fun updateOutputSubDirs() {
        val includeRoot = mainFile?.parent
        val outPath = virtualFile?.path ?: return

        val files: Set<PsiFile>
        try {
            files = mainFile?.psiFile(project)?.referencedFileSet() ?: emptySet()
        }
        catch (e: IndexNotReadyException) {
            throw ExecutionException("Please wait until the indices are built.", e)
        }

        // Create output paths (see issue #70 on GitHub)
        files.asSequence()
            .mapNotNull { FileUtil.pathRelativeTo(includeRoot?.path ?: return@mapNotNull null, it.virtualFile.parent.path) }
            .forEach { File(outPath + it).mkdirs() }
    }
}