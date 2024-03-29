package dev.lunarcoffee.risako.framework.core.commands.transformers

import dev.lunarcoffee.risako.framework.core.commands.CommandContext
import dev.lunarcoffee.risako.framework.core.std.*

class TrInt(
    override val optional: Boolean = false,
    override val default: Int = 0
) : Transformer<Int> {

    override suspend fun transform(ctx: CommandContext, args: MutableList<String>): OpResult<Int> {
        // Try and return the first value of [args] as an [Int], then try to return [default] if
        // [optional] is true.
        return args
            .firstOrNull()
            ?.toIntOrNull()
            ?.run {
                args.removeAt(0)
                OpSuccess(this)
            } ?: if (optional) OpSuccess(default) else OpError()
    }
}
