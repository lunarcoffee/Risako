package framework.core.bot

import framework.core.bot.config.BotConfig
import framework.core.commands.Command
import framework.core.dispatchers.Dispatcher
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.hooks.EventListener

internal interface Bot {
    val jda: JDA
    val config: BotConfig
    val dispatcher: Dispatcher

    val commands: List<Command>
    val commandNames: List<String>

    val listeners: List<EventListener>
    val listenerNames: List<String>

    fun addCommand(command: Command)
    fun addListener(listener: EventListener)

    fun loadAllCommands()
}