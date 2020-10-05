buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.4.0"))
        classpath("com.android.tools.build:gradle:4.0.1")
    }
}

plugins {
    id("org.jmailen.kotlinter") version "3.2.0" apply false
}

subprojects {
    repositories {
        google()
        jcenter()
    }
}
