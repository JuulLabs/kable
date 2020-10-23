buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.4.0"))
    }
}

plugins {
    id("org.jmailen.kotlinter") version "3.2.0" apply false
}

subprojects {
    repositories {
        jcenter()
    }
}
