fun coroutines(
    module: String = "core",
    version: String = "1.5.2"
): String = "org.jetbrains.kotlinx:kotlinx-coroutines-$module:$version"

fun atomicfu(
    module: String,
    version: String = "0.16.3"
) = "org.jetbrains.kotlinx:atomicfu-$module:$version"

fun uuid(
    artifact: String = "uuid",
    version: String = "0.3.1"
): String = "com.benasher44:$artifact:$version"

fun stately(
    module: String,
    version: String = "1.1.10-a1"
): String = "co.touchlab:stately-$module:$version"

fun wrappers(
    version: String = "1.0.1-pre.264-kotlin-1.5.31"
) = "org.jetbrains.kotlin-wrappers:kotlin-extensions:$version"

object androidx {
    fun startup(
        version: String = "1.1.0"
    ) = "androidx.startup:startup-runtime:$version"
}

fun tuulbox(
    module: String,
    version: String = "4.8.0"
) = "com.juul.tuulbox:$module:$version"
