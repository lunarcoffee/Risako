package dev.lunarcoffee.risako.bot.exts.commands.owner.ex.executors

import dev.lunarcoffee.risako.bot.exts.commands.owner.ex.ExecResult
import dev.lunarcoffee.risako.framework.api.extensions.*
import dev.lunarcoffee.risako.framework.core.commands.CommandContext
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

class ShellScriptExecutor(private val script: String) : CodeExecutor {
    override suspend fun execute(ctx: CommandContext): ExecResult {
        File("$SCRIPT_ROOT/script.sh").writeText("#!/bin/bash\n$script")

        var process: Process? = null
        val fileOut = File("$SCRIPT_ROOT/out.txt")
        val fileErr = File("$SCRIPT_ROOT/err.txt")

        val time = measureNanoTime {
            try {
                process = ProcessBuilder("bash", "$SCRIPT_ROOT/script.sh")
                    .redirectOutput(ProcessBuilder.Redirect.to(fileOut))
                    .redirectError(ProcessBuilder.Redirect.to(fileErr))
                    .start()
                    .apply { waitFor(10, TimeUnit.SECONDS) }
            } catch (e: IOException) {
                ctx.sendError("Error starting process! Check your PMs for details.")
                ctx.event.author.openPrivateChannel().await().send("```$e```")

                return ExecResult.ERROR
            }
        } / 1_000_000

        // Get correct shell environment name for the OS. Not sure why this is even here, since the
        // actual execution part only supports bash. Maybe I'll do something with that later.
        val osName = System.getProperty("os.name")
        val nameOfExecutor = when {
            "Windows" in osName -> "Windows PowerShell 6.1"
            "Linux" in osName -> "GNU Bash 5.0.3"
            else -> "Unknown Shell Environment"
        }

        val stdout = "\n${fileOut.readText().trim()}".ifBlank { "" }
        val stderr = "\n${fileErr.readText().trim()}".ifBlank { "" }

        return ExecResult(
            nameOfExecutor,
            stdout,
            stderr,
            process!!.exitValue(),
            time
        )
    }

    companion object {
        private const val SCRIPT_ROOT = "src/main/resources/ex/sh"
    }
}
