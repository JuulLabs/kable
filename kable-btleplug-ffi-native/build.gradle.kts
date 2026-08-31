plugins {
    alias(libs.plugins.maven.publish)
    kotlin("multiplatform")
    alias(libs.plugins.ubique.uniffi)
}

fun isRunningOnWindows() = System.getProperty("os.name").orEmpty().lowercase().startsWith("windows")

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    if (isRunningOnWindows()) {
        mingwX64()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

cargo {
    packageDirectory = rootProject.layout.projectDirectory.dir("kable-btleplug-ffi")
}

uniffi {
    // Pin the bindgen to the same version as the plugin (the plugin's default is the repository's
    // default branch).
    bindgenFromGitTag(
        repository = "https://github.com/UbiqueInnovation/uniffi-kotlin-multiplatform-bindings.git",
        tag = "v1.2.1",
    )
    generateFromLibrary()
}

// On Windows, `cargo install` produces `uniffi-bindgen-kotlin-multiplatform.exe`, but the plugin
// resolves the installed binary without the `.exe` extension. Provide the binary under the name
// the plugin expects. https://github.com/UbiqueInnovation/uniffi-kotlin-multiplatform-bindings/issues
if (isRunningOnWindows()) {
    val bindgenBinDirectory = layout.buildDirectory.dir("uniffi/bindgen/bin")
    val fixBindgenExeName = tasks.register<Copy>("fixBindgenExeName") {
        dependsOn("installBindgen")
        from(bindgenBinDirectory.map { it.file("uniffi-bindgen-kotlin-multiplatform.exe") })
        into(bindgenBinDirectory)
        rename { "uniffi-bindgen-kotlin-multiplatform" }
    }
    tasks.named("buildBindings") {
        dependsOn(fixBindgenExeName)
    }

    // The generated cinterop `.def` files contain absolute paths using backslashes, which the
    // cinterop tool rejects ("Malformed \uxxxx encoding", as `.def` files are parsed as Java
    // properties files). Rewrite them to forward slashes. Also drop the `-lwindows.X.Y.Z` linker
    // flag: the `windows` crate's import library is bundled into the Rust static library already,
    // and no search path for it is emitted (which fails the final link).
    tasks.matching { it.name.startsWith("generateDefFileFor") }.configureEach {
        doLast {
            outputs.files.files
                .filter { it.isFile && it.extension == "def" }
                .forEach { file ->
                    file.writeText(
                        file.readText()
                            .replace('\\', '/')
                            .replace(Regex("""-lwindows\.[0-9.]+ """), ""),
                    )
                }
        }
    }
}
