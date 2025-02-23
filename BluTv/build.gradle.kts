import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "kurd.reco.blutv"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    tasks.register("generateJson") {
        doFirst {
            // Change these
            val packageName = project.android.namespace
            val className = "BluApi"
            val pluginName = "BluTv"
            val pluginId = className.lowercase()

            val jsonObject = """
            {
                "package": "$packageName",
                "package_name": "$className",
                "plugin_id": "$pluginId",
                "plugin_name": "$pluginName"
            }
        """.trimIndent()

            val manifestFile = file("${project.projectDir.absolutePath}/src/main/manifest.json")
            manifestFile.writeText(jsonObject)
        }
    }

    tasks.register("makeDex") {
        dependsOn("generateJson")
        doLast {
            val extLoc = file("${project.projectDir.absolutePath}/build/outputs/aar/${project.name}-debug.aar")
            val buildFolder = file("${project.projectDir.absolutePath}/build")

            project.copy {
                from(zipTree(extLoc)) {
                    exclude("**/R.txt", "**/proguard.txt", "**/AndroidManifest.xml","**/META-INF/**")
                }
                into(buildFolder)
            }

            val d8 = "${sdkDirectory.path}/build-tools/${buildToolsVersion}/d8.bat"
            val buildPath = "${project.projectDir.absolutePath}/build/"

            val listArgs = listOf(d8, "--release", "--output", "${buildPath}${project.projectDir.name}.zip", "${buildPath}classes.jar")
            project.exec {
                commandLine(listArgs)
            }

            val manifest = file("${project.projectDir.absolutePath}/src/main/manifest.json")
            if (manifest.exists()) {
                val extension = "${buildPath}${project.projectDir.name}.zip"
                FileInputStream(extension).use { fis ->
                    ZipInputStream(fis).use { zis ->
                        val newName = file("${buildPath}${project.projectDir.name}.krd")
                        ZipOutputStream(FileOutputStream(newName)).use { zOut ->
                            val buff = ByteArray(1024)
                            var bytes: Int
                            var entry: ZipEntry?
                            while (zis.nextEntry.also { entry = it } != null) {
                                zOut.putNextEntry(entry)
                                while (zis.read(buff).also { bytes = it } != -1) {
                                    zOut.write(buff, 0, bytes)
                                }
                                zOut.closeEntry()
                            }
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            FileInputStream(manifest).use { input ->
                                entry = ZipEntry(manifest.name)
                                zOut.putNextEntry(entry)
                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    zOut.write(buffer, 0, bytesRead)
                                }
                                zOut.closeEntry()
                            }
                        }
                    }
                }
                File(extension).delete()
            }
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.jackson.annotations)
    implementation(libs.nicehttp)
    implementation(libs.rokko.api)
}