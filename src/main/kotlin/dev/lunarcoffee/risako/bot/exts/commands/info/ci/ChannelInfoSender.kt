package dev.lunarcoffee.risako.bot.exts.commands.info.ci

import dev.lunarcoffee.risako.bot.consts.Emoji
import dev.lunarcoffee.risako.bot.consts.TIME_FORMATTER
import dev.lunarcoffee.risako.bot.toYesNo
import dev.lunarcoffee.risako.framework.api.dsl.embed
import dev.lunarcoffee.risako.framework.api.extensions.send
import dev.lunarcoffee.risako.framework.core.commands.CommandContext
import dev.lunarcoffee.risako.framework.core.std.ContentSender
import dev.lunarcoffee.risako.framework.core.std.SplitTime
import net.dv8tion.jda.api.entities.*

class ChannelInfoSender(private val channel: GuildChannel) : ContentSender {
    override suspend fun send(ctx: CommandContext) {
        ctx.send(if (channel is TextChannel) textChannelEmbed() else voiceChannelEmbed())
    }

    private fun textChannelEmbed(): MessageEmbed {
        return embed {
            (channel as TextChannel).run {
                val slowmode = if (slowmode != 0)
                    SplitTime(slowmode.toLong()).toString()
                else
                    "(none)"

                title = "${Emoji.MAG_GLASS}  Info on text channel **#$name**:"
                description = """
                    |**Channel ID**: $id
                    |**Server**: ${guild.name}
                    |**Topic**: ${topic ?: "(none)"}
                    |**Slowmode**: $slowmode
                    |**NSFW**: ${isNSFW.toYesNo()}
                    |**Mention**: $asMention
                    |**Category**: ${parent?.name ?: "(none)"}
                    |**Creation time**: ${timeCreated.format(TIME_FORMATTER)}
                """.trimMargin()
            }
        }
    }

    private fun voiceChannelEmbed(): MessageEmbed {
        return embed {
            (channel as VoiceChannel).run {
                val limit = if (userLimit == 0) "(none)" else "$userLimit users"

                title = "${Emoji.MAG_GLASS}  Info on voice channel **#$name**:"
                description = """
                    |**Channel ID**: $id
                    |**Server**: ${guild.name}
                    |**Bitrate**: ${bitrate / 1_000}kb/s
                    |**User limit**: $limit
                    |**Mention**: <#$id>
                    |**Category**: ${parent?.name ?: "(none)"}
                    |**Creation time**: ${timeCreated.format(TIME_FORMATTER)}
                """.trimMargin()
            }
        }
    }
}
