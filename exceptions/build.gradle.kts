plugins {
    kotlin("multiplatform")
    id("org.jmailen.kotlinter")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

/* ```
 *   common
 *   |-- js
 *   |-- jvm
 *   '-- apple
 *       |-- ios
 *       '-- macos
 * ```
 */
kotlin {
    explicitApi()

    jvm()
    js().browser()
    iosX64()
    macosArm64()
    iosArm32()
    iosArm64()
    iosSimulatorArm64()
    macosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val appleMain by creating {
            dependsOn(commonMain)
        }

        val appleTest by creating

        val macosX64Main by getting {
            dependsOn(appleMain)
        }

        val macosX64Test by getting {
            dependsOn(appleTest)
        }

        val macosArm64Main by getting {
            dependsOn(appleMain)
        }

        val macosArm64Test by getting {
            dependsOn(appleTest)
        }

        val iosX64Main by getting {
            dependsOn(appleMain)
        }

        val iosX64Test by getting {
            dependsOn(appleTest)
        }

        val iosArm32Main by getting {
            dependsOn(appleMain)
        }

        val iosArm32Test by getting {
            dependsOn(appleTest)
        }

        val iosArm64Main by getting {
            dependsOn(appleMain)
        }

        val iosArm64Test by getting {
            dependsOn(appleTest)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(appleMain)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(appleTest)
        }
    }
}
