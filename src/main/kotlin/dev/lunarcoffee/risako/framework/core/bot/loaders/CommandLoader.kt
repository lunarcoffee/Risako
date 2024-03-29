package dev.lunarcoffee.risako.framework.core.bot.loaders

import dev.lunarcoffee.risako.framework.core.annotations.CommandGroup
import dev.lunarcoffee.risako.framework.core.bot.Bot
import dev.lunarcoffee.risako.framework.core.commands.Command

class CommandLoader(override val bot: Bot) : ComponentClassLoader() {
    private val commandGroups = loadClasses(bot.config.commandP)
        .filter { c -> c.annotations.any { it.annotationClass == CommandGroup::class } }

    // Map of [CommandGroups] to their commands.
    val groupToCommands = commandGroups
        .map { group -> group.methods.filter { it.returnType == Command::class.java } }
        .zip(commandGroups.map { callConstructorWithBot(it)!! })
        .associate { (methods, group) ->
            val annotation = group::class.annotations.find { it is CommandGroup } as CommandGroup
            annotation to methods.mapNotNull {
                if (it.returnType != Command::class.java)
                    return@mapNotNull null
                (it.invoke(group) as Command).apply { groupName = annotation.name }
            }
        }

    val commands = groupToCommands.values.flatten().toMutableList()
}