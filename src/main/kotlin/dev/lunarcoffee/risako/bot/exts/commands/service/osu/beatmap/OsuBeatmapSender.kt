package dev.lunarcoffee.risako.bot.exts.commands.service.osu.beatmap

import dev.lunarcoffee.risako.bot.consts.Emoji
import dev.lunarcoffee.risako.framework.api.dsl.embed
import dev.lunarcoffee.risako.framework.api.dsl.embedPaginator
import dev.lunarcoffee.risako.framework.api.extensions.send
import dev.lunarcoffee.risako.framework.api.extensions.sendError
import dev.lunarcoffee.risako.framework.core.commands.CommandContext
import dev.lunarcoffee.risako.framework.core.std.ContentSender

class OsuBeatmapSender(private val id: String, private val mode: Int) : ContentSender {
    override suspend fun send(ctx: CommandContext) {
        val beatmaps = OsuBeatmapRequester(id, mode).get()
        if (beatmaps == null) {
            ctx.sendError("I can't find a beatmap with that ID! Maybe try a different gamemode?")
            return
        }

        ctx.send(
            embedPaginator(ctx.event.author) {
                for (beatmap in beatmaps) {
                    page(
                        embed {
                            beatmap.run {
                                val link = "https://osu.ppy.sh/beatmapsets/$id#$modeUrl/$beatmapId"

                                title = "${Emoji.WORLD_MAP}  Info on beatmap set **$name**:"
                                description = """
                                    |**Beatmap ID**: $beatmapId
                                    |**Mode**: $modeName
                                    |**Creator**: $creator
                                    |**Music artist**: $artist
                                    |**Difficulty**: $starRating★
                                    |**BPM**: $bpm
                                    |**Length**: $length
                                    |**Status**: $status
                                    |**CS/AR/HP/OD**: $cs/$ar/$hp/$od
                                    |**Maximum combo**: ${maxCombo ?: "(not applicable)"}
                                    |**Link**: [beatmap link]($link)
                                """.trimMargin()
                            }
                        }
                    )
                }
            }
        )
    }
}
