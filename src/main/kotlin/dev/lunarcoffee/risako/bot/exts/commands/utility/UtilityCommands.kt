@file:Suppress("unused")

package dev.lunarcoffee.risako.bot.exts.commands.utility

import dev.lunarcoffee.risako.bot.exts.commands.utility.help.HelpDetailSender
import dev.lunarcoffee.risako.bot.exts.commands.utility.help.HelpListSender
import dev.lunarcoffee.risako.bot.exts.commands.utility.remind.ReminderManager
import dev.lunarcoffee.risako.bot.exts.commands.utility.steal.EmoteStealerSender
import dev.lunarcoffee.risako.bot.exts.commands.utility.tags.*
import dev.lunarcoffee.risako.framework.api.dsl.command
import dev.lunarcoffee.risako.framework.api.extensions.*
import dev.lunarcoffee.risako.framework.core.annotations.CommandGroup
import dev.lunarcoffee.risako.framework.core.bot.Bot
import dev.lunarcoffee.risako.framework.core.commands.transformers.*
import dev.lunarcoffee.risako.framework.core.std.OpSuccess
import dev.lunarcoffee.risako.framework.core.std.SplitTime

@CommandGroup("Utility")
class UtilityCommands(private val bot: Bot) {
    fun emote() = command("emote") {
        description = "Sends one or more custom emotes from any server I am in."
        aliases = arrayOf("sendemote")
        deleteSender = true

        extDescription = """
            |`$name names...`\n
            |Sends one or more emotes to the command user's channel. If an emote is not found, I
            |simply don't send that one.
        """

        expectedArgs = arrayOf(TrSplit())
        execute { args ->
            val emoteNames = args.get<List<String>>(0)
            val emotes = emoteNames
                .mapNotNull { jda.getEmotesByName(it, true).firstOrNull()?.asMention }
                .joinToString(" ")

            if (emotes.isEmpty()) {
                sendError("I don't have any of those emotes!")
                return@execute
            }

            // Cannot use [sendAsAuthor] since webhooks cannot access custom emotes.
            send(emotes)
        }
    }

    fun steal() = command("steal") {
        description = "Gets custom emotes from the history of the current channel."
        aliases = arrayOf("stealemotes")

        extDescription = """
            |`$name [limit]`\n
            |Steals custom emotes from the current channel's history. If `limit` is specified, this
            |command will attempt to steal all emotes from the past `limit` messages. If not, the
            |default is the past 100 messages. Stealing just means getting the image links for each
            |emote, not anything remotely illegal or suspicious. Yeah.
            |
        """

        expectedArgs = arrayOf(TrInt(true, 100))
        execute { args ->
            val limit = args.get<Int>(0)
            if (limit !in 1..1_000) {
                sendError("I can't steal from that many messages in history!")
                return@execute
            }
            send(EmoteStealerSender(limit))
        }
    }

    fun remind() = command("remind") {
        description = "Sets a reminder so you don't have to remember things!"
        aliases = arrayOf("remindme")

        extDescription = """
            |`$name time [reason]`\n
            |This command takes a time string that looks something like `3h 40m` or `1m 30s` or
            |`2d 4h 32m 58s`, and optionally, a reason to remind you of. After the amount of time
            |specified in `time`, I should ping you in the channel you send the command in and
            |remind you of what you told me.
        """

        expectedArgs = arrayOf(TrTime(), TrRest(true, "(no reason)"))
        execute { args ->
            val time = args.get<SplitTime>(0)
            val reason = args.get<String>(1)
            val dateTime = time.localWithoutWeekday().replace(" at ", "` at `")

            sendSuccess("I'll remind you on `$dateTime`!")
            ReminderManager(this).scheduleReminder(time, reason)
        }
    }

    fun remindl() = command("remindl") {
        val allowedOperations = arrayOf("list", "cancel")

        description = "Lets you view and cancel your reminders."
        aliases = arrayOf("remindlist")

        extDescription = """
            |`$name [list|cancel] [id|range]`\n
            |This command is for managing reminders made by the `remind` command. You can view and
            |cancel any of your reminders here.
            |&{Viewing reminders:}
            |Seeing your active reminders is easy. Just use the command without arguments (i.e.
            |`..remindlist`), and I will list out all of your active reminders. Each entry will
            |have the reminder's reason and the time it will be fired at.
            |&{Cancelling reminders:}
            |Reminder cancellation is also easy. The first argument must be `cancel`, and the
            |second argument can be either a number or range of numbers (i.e. `1-5` or `4-6`). I
            |will cancel the reminders with the IDs you specify (either `id` or `range`).
        """

        expectedArgs = arrayOf(TrWord(true, "list"), TrWord(true))
        execute { args ->
            val operation = args.get<String>(0)
            val idOrRange = args.get<String>(1)

            if (operation !in allowedOperations) {
                sendError("That isn't a valid operation!")
                return@execute
            }

            // This command lets users remove either a single reminder or reminders within a range
            // of IDs. This here tries to use the input as a range first, then as a single number.
            val potentialId = idOrRange.toIntOrNull()
            val range = TrIntRange(true).transform(this, mutableListOf(idOrRange)).run {
                when {
                    this is OpSuccess -> when {
                        this.result == 0..0 && potentialId != null -> potentialId..potentialId
                        this.result == 0..0 && operation != "list" -> {
                            sendError("That isn't a valid number or range!")
                            return@execute
                        }
                        else -> this.result
                    }
                    // Will never be [OpError] because the argument is optional.
                    else -> throw IllegalStateException()
                }
            }

            if (operation == "list")
                ReminderManager(this).sendRemindersEmbed()
            else if (operation == "cancel")
                ReminderManager(this).cancelReminders(range)
        }
    }

    fun tags() = command("tags") {
        description = "Create textual tags to save!"
        aliases = arrayOf("notes")

        extDescription = """
            |`$name [action] [tag name] [tag content] [-r]`\n
            |This command lets you save information in tags. These tags have a name and can store
            |a lot of content. They also store the person who created them and the time at which
            |the tag was created.
            |&{Viewing tags:}
            |To view all the tags on the current server, use the command with no arguments (as in
            |`..$name` only). To view a specific tag, action should be `view` and `tag name` should
            |be the name of the tag you want to view. This will send a fancy embed with the author
            |and tag creation time. If you want only the tag content, add `-r` at the end.
            |&{Adding tags:}
            |To add a tag, `action` should be `add`, `tag name` should be the name you want to
            |give the tag, and `tag content` should be the content of the tag. The name cannot be
            |longer than 30 characters, and the content cannot be longer than 1000 characters.
            |&{Editing tags:}
            |If you ever want to change one of your tags, you can do so by making `action` the word
            |`edit`, `tag name` the name of the tag you want to edit (you have to have created it),
            |and `tag content` the updated content of the tag.
            |&{Deleting tags:}
            |To remove a tag, `action` has to be `delete`, and `tag name` has to be the name of the
            |tag you want to delete (which has to have been created by you).
        """

        expectedArgs = arrayOf(TrWord(true), TrWord(true), TrRest(true))
        execute { args ->
            val action = args.get<String>(0)
            if (action.isEmpty()) {
                send(TagListSender())
                return@execute
            }

            val tagName = args.get<String>(1)
            val tagContentOrRawFlag = args.get<String>(2)

            TagManager(this).run {
                when (action) {
                    "view" -> send(SingleTagSender(tagName, tagContentOrRawFlag == "-r"))
                    "add" -> addTag(tagName, tagContentOrRawFlag)
                    "edit" -> editTag(tagName, tagContentOrRawFlag)
                    "delete" -> deleteTag(tagName)
                    else -> sendError("That isn't a valid operation!")
                }
            }
        }
    }

    fun help() = command("help") {
        description = "Lists all commands or shows help for a specific command."
        extDescription = """
            |`$name [command name]`\n
            |With a command name, this command gets its aliases, expected usage, expected
            |arguments, and n extended description. Otherwise, this command simply lists all of the
            |available commands, as well as short descriptions of the commands in each category.
            |&{Reading command usages:}
            |The syntax of the expected command usage is as follows:\n
            | - `name`: denotes that `name` is required, which may be literal or variable\n
            | - `name1|name2`: denotes that either `name1` or `name2` is valid\n
            | - `name...`: denotes that many of `name` can be specified\n
            |If an argument is wrapped with square brackets, it is optional. You may wrap an
            |argument with double quotes "like this" to treat it as one instead of multiple.
        """

        expectedArgs = arrayOf(TrWord(true))
        execute { args ->
            val commandName = args.get<String>(0)
            val command = bot.commands.find { commandName in it.names }

            if (commandName.isNotBlank() && command == null) {
                sendError("I can't find that command!")
                return@execute
            }
            send(if (command == null) HelpListSender() else HelpDetailSender(command))
        }
    }
}
