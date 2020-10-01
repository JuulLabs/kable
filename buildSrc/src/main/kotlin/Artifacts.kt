fun coroutines(
    module: String,
    version: String = "1.4.0-M1"
): String = "org.jetbrains.kotlinx:kotlinx-coroutines-$module:$version"

fun uuid(
    artifact: String = "uuid",
    version: String = "0.2.2"
): String = "com.benasher44:$artifact:$version"
