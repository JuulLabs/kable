plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.plugin)
}

gradlePlugin {
    plugins {
        create("uniffiKotlin") {
            id = "com.juul.kable.uniffi"
            displayName = "uniffi-kotlin"
            implementationClass = "com.juul.kable.uniffi.plugin.UniffiKotlinPlugin"
        }
    }
}
