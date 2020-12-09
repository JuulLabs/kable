fun coroutines(
    module: String,
    version: String = "1.4.2"
): String = "org.jetbrains.kotlinx:kotlinx-coroutines-$module:$version"

fun atomicfu(
    module: String,
    version: String = "0.14.4"
) = "org.jetbrains.kotlinx:atomicfu-$module:$version"

fun uuid(
    artifact: String = "uuid",
    version: String = "0.2.2"
): String = "com.benasher44:$artifact:$version"

fun stately(
    module: String,
    version: String = "1.1.1-a1"
): String = "co.touchlab:stately-$module:$version"

object androidx {
    fun startup(
        version: String = "1.0.0"
    ) = "androidx.startup:startup-runtime:$version"
}
