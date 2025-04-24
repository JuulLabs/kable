plugins {
    kotlin("multiplatform")
}

// TODO: this currently is only configured for desktop mac (.dylib), but should
//       also support linux (.so) and windows (.dll) targets. Ideally, all of
//       those can build from every platform.

kotlin {
    // Uniffi-bindgen is not explicitApi() compatible
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    jvm()

    sourceSets {
        jvmMain {
            kotlin.srcDir("build/generated/uniffi/kotlin")
            dependencies {
                api(libs.jna)
                api(libs.kotlinx.coroutines.core)
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

fun Task.commonRustSetup() {
    group = "rust"

    if (this is Exec) {
        inputs.dir("src/commonMain/rust")
        inputs.file("Cargo.lock")
        inputs.file("Cargo.toml")
        inputs.file("uniffi.toml")
    }
}

tasks.register<Exec>("cargoClean") {
    commonRustSetup()
    commandLine("cargo", "clean")
}

tasks.register<Exec>("cargoCheck") {
    commonRustSetup()
    commandLine("cargo", "check")
}

tasks.register<Exec>("cargoTest") {
    commonRustSetup()
    commandLine("cargo", "test")
}

tasks.register<Exec>("cargoBuild") {
    commonRustSetup()
    commandLine("cargo", "build", "--release")
}

tasks.register<Exec>("cargoLintClippy") {
    commonRustSetup()
    commandLine("cargo", "fmt", "--check")
}

tasks.register<Exec>("cargoLintRust") {
    commonRustSetup()
    commandLine("cargo", "fmt", "--check")
}

tasks.register("cargoLint") {
    commonRustSetup()
    dependsOn("cargoLintClippy", "cargoLintRust")
}

tasks.register<Exec>("cargoFormat") {
    commonRustSetup()
    commandLine("cargo", "fmt")
}

tasks.register<Exec>("cargoUniffiBindgen") {
    commonRustSetup()
    dependsOn("cargoBuild", "cleanUniffiBindgen")
    commandLine(
        "cargo",
        "run",
        "--bin",
        "uniffi-bindgen",
        "generate",
        "--library",
        "target/release/libbtleplug_ffi.dylib",
        "--language",
        "kotlin",
        "--out-dir",
        "build/generated/uniffi/kotlin",
        "--no-format",
    )
}

tasks.register<Exec>("cleanUniffiBindgen") {
    commonRustSetup()
    workingDir("build/generated/uniffi")
    commandLine("rm", "-rf", "./kotlin")
}

tasks.named("allTests") {
    dependsOn("cargoTest")
}

tasks.named("clean") {
    dependsOn("cargoClean", "cleanUniffiBindgen")
}

tasks.named("compileKotlinJvm") {
    dependsOn("cargoUniffiBindgen")
}

tasks.named<Copy>("jvmProcessResources") {
    dependsOn("cargoUniffiBindgen")

    from(project.file("target/release")) {
        include("libbtleplug_ffi.dylib")
    }
}
