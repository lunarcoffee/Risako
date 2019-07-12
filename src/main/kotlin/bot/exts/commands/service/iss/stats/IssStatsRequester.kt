package bot.exts.commands.service.iss.stats

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import bot.consts.GSON
import framework.core.std.Requester

internal class IssStatsRequester : Requester<IssStats> {
    override suspend fun get(): IssStats {
        return GSON.fromJson(Fuel.get(URL).awaitStringResult().get(), IssStats::class.java)
    }

    companion object {
        private const val URL = "https://api.wheretheiss.at/v1/satellites/25544"
    }
}