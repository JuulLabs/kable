fun coroutines(
    module: String,
    version: String = "1.4.0"
): String = "org.jetbrains.kotlinx:kotlinx-coroutines-$module:$version"

fun atomicfu(
    module: String,
    version: String = "0.14.4"
) = "org.jetbrains.kotlinx:atomicfu-$module:$version"

fun uuid(
    artifact: String = "uuid",
    version: String = "0.2.2"
): String = "com.benasher44:$artifact:$version"
