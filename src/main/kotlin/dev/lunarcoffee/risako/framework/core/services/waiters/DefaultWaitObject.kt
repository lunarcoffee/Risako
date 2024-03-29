package dev.lunarcoffee.risako.framework.core.services.waiters

import kotlinx.coroutines.CompletableDeferred
import net.dv8tion.jda.api.entities.Message

class DefaultWaitObject(
    override val userId: String,
    override val channelId: String,
    override val message: CompletableDeferred<Message>
) : WaitObject
