@file:Suppress("unused")

package dev.lunarcoffee.risako.bot.exts.listeners

import dev.lunarcoffee.risako.bot.consts.Emoji
import dev.lunarcoffee.risako.framework.api.extensions.sendSuccess
import dev.lunarcoffee.risako.framework.core.annotations.ListenerGroup
import dev.lunarcoffee.risako.framework.core.bot.Bot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

@ListenerGroup
class MentionListeners(
    private val bot: Bot
) : ListenerAdapter(), CoroutineScope by CoroutineScope(Dispatchers.IO) {
    
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val content = event.message.contentRaw.trim()

        // React to the message if it contained a mention to the owner or the bot.
        if ("<@${bot.config.ownerId}>" in content || "<@!${bot.config.ownerId}>" in content)
            event.message.addReaction(Emoji.COFFEE).queue()

        // Help the user that couldn't read the activity text by sending them the prefix. :P
        if (content == "<@${bot.jda.selfUser.id}>" || content == "<@!${bot.jda.selfUser.id}>")
            launch { event.channel.sendSuccess("My prefix here is `..`!") }
    }
}
