plugins {
    kotlin("multiplatform")
}

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

data class Target(
    val name: String,
    val triple: String,
    val libraryName: String,
)

val targets = run {
    val currentOs = run {
        val name = System.getProperty("os.name").lowercase()
        when {
            "mac" in name -> "apple"
            "windows" in name -> "windows"
            else -> "linux"
        }
    }
    val currentArch = run {
        val arch = System.getProperty("os.arch").lowercase()
        when {
            "arm" in arch || "aarch" in arch -> "aarch64"
            else -> "x86_64"
        }
    }
    listOf(
        Target("MacosArm64", "aarch64-apple-darwin", "libbtleplug_ffi.dylib"),
        Target("MacosX64", "x86_64-apple-darwin", "btleplug_ffi.dylib"),
        Target("WindowsX64", "x86_64-pc-windows-msvc", "btleplug_ffi.dll"),
        Target("LinuxX64", "x86_64-unknown-linux-gnu", "libbtleplug_ffi.so"),
        Target("LinuxArm64", "aarch64-unknown-linux-gnu", "libbtleplug_ffi.so"),
    ).filter { target ->
        currentOs.contains("mac") || (currentOs in target.triple && currentArch in target.triple)
    }
}

targets.forEach { target ->
    tasks.register<Exec>("cargoInstallTarget${target.name}") {
        commonRustSetup()
        commandLine("rustup", "target", "add", target.triple)
        onlyIf { !File("~/.rustup/toolchains/stable-${target.triple}").exists() }
    }

    tasks.register<Exec>("cargoBuild${target.name}") {
        commonRustSetup()
        dependsOn("cargoInstallTarget${target.name}")
        outputs.dir("target/${target.triple}/release")
        commandLine(
            "cargo", "build",
            "--release",
            "--target", target.triple,
        )
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
    dependsOn(targets.map { "cargoBuild${it.name}" })
    outputs.dir("target")
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
    dependsOn("cargoBuild")
    doFirst { project.delete("build/generated/uniffi/kotlin") }
    val input = "target/${targets.first().triple}/release/${targets.first().libraryName}"
    inputs.file(input)
    outputs.dir("build/generated/uniffi/kotlin")
    commandLine(
        "cargo",
        "run",
        "--release",
        "--bin",
        "uniffi-bindgen",
        "generate",
        "--library",
        input,
        "--language",
        "kotlin",
        "--out-dir",
        "build/generated/uniffi/kotlin",
        "--no-format",
    )
}

tasks.named("allTests") {
    dependsOn("cargoTest")
}

tasks.named("clean") {
    dependsOn("cargoClean")
}

tasks.named("compileKotlinJvm") {
    dependsOn("cargoUniffiBindgen")
}

tasks.named<Copy>("jvmProcessResources") {
    dependsOn("cargoBuild")

    targets.forEach { target ->
        from(project.file("target/${target.triple}/release")) {
            include(target.libraryName)
            val (os, arch) = when (target.name) {
                "MacosArm64" -> "darwin" to "aarch64"
                "MacosX64" -> "darwin" to "x86-64"
                "WindowsX64" -> "win32" to "x86-64"
                "LinuxX64" -> "linux" to "x86-64"
                "LinuxArm64" -> "linux" to "aarch64"
                else -> error("Unknown target: ${target.name}")
            }
            into("${os}-${arch}")
        }
    }
}
