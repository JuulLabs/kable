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

data class Target(
    val name: String,
    val triple: String,
    val libraryName: String,
)

val targets = listOf(
    Target("MacosArm64", "aarch64-apple-darwin", "libbtleplug_ffi.dylib"),
    Target("MacosX64", "x86_64-apple-darwin", "btleplug_ffi.dylib"),
    Target("WindowsX64", "x86_64-pc-windows-msvc", "btleplug_ffi.dll"),
    Target("LinuxX64", "x86_64-unknown-linux-gnu", "libbtleplug_ffi.so"),
    Target("LinuxArm64", "aarch64-unknown-linux-gnu", "libbtleplug_ffi.so"),
).filter { target ->
    System.getProperty("buildDistribution") == "true" ||
            (currentOs in target.triple && currentArch in target.triple)
}

val currentTarget = targets.single { target ->
    currentOs in target.triple && currentArch in target.triple
}

targets.forEach { target ->
    tasks.register<Exec>("rustupTargetAdd${target.name}") {
        commonRustSetup()
        onlyIf { !File("${System.getProperty("user.home")}/.rustup/toolchains/stable-${target.triple}").exists() }
        if (target == currentTarget) {
            commandLine("rustup", "target", "add", target.triple)
        } else {
            commandLine(
                "rustup",
                "toolchain",
                "add",
                "stable-${target.triple}",
                "--profile",
                "minimal",
                "--force-non-host",
            )
        }
    }

    val executable = if (target == currentTarget) "cargo" else "cross"
    tasks.register<Exec>("${executable}Build${target.name}") {
        commonRustSetup()
        if (target != currentTarget) {
            dependsOn("cargoInstallCross")
            dependsOn("rustupTargetAdd${target.name}")
        }
        outputs.dir("target/${target.triple}/release")
        commandLine(
            executable,
            "build",
            "--release",
            "--target", target.triple,
        )
    }
}

tasks.register<Exec>("cargoInstallCross") {
    commonRustSetup()
    onlyIf {
        val extension = if (currentOs == "windows") ".exe" else ""
        !File("${System.getProperty("user.home")}/.cargo/bin/cross$extension").exists()
    }
    commandLine("cargo", "install", "cross")
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
    dependsOn(
        targets.map { target ->
            val executable = if (target == currentTarget) "cargo" else "cross"
            "${executable}Build${target.name}"
        },
    )
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
    dependsOn("cargoBuild${currentTarget.name}")
    doFirst { project.delete("build/generated/uniffi/kotlin") }
    val input = "target/${currentTarget.triple}/release/${currentTarget.libraryName}"
    inputs.file(input)
    outputs.dir("build/generated/uniffi/kotlin")
    commandLine(
        "cargo",
        "run",
        "--release",
        "--target",
        currentTarget.triple,
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
