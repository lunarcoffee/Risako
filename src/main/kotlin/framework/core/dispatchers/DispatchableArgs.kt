package framework.core.dispatchers

interface DispatchableArgs {
    val items: List<Any?>

    fun <T> get(index: Int): T
}