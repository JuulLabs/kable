import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
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

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    languageVersion.set(KotlinVersion.KOTLIN_2_0)
}
