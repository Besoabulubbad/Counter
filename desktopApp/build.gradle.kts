import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

tasks.register<JavaExec>("screenshots") {
    group = "verification"
    description = "Render the Expanded/Medium/Compact layouts to PNGs off-screen"
    mainClass.set("com.abulubad.counter.ScreenshotsKt")
    classpath = sourceSets["main"].runtimeClasspath
}

compose.desktop {
    application {
        mainClass = "com.abulubad.counter.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Counter"
            packageVersion = System.getenv("APP_VERSION") ?: "1.0.0"
        }
    }
}