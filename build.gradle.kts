import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
}

group = "me.tooster"
version = "w2z3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "me.tooster.w2.Z3"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }

}


val solutions = File(projectDir,"src/main/kotlin/me/tooster").walk()
    .mapNotNull { Regex(""".*(w\d+)/(z\d+).*\.kt""").matchEntire(it.path) }.toList()

solutions.forEach {
    val (w, z) = it.groupValues[1] to it.groupValues[2]
    tasks.register<Jar>("${w}${z}") {
        group = "build solutions"
        archiveVersion.set("$w$z")


        manifest{
            attributes["Main-Class"] = "me.tooster.$w.${z.capitalize()}"
        }

        from(sourceSets["main"].output)
        // todo: resources if needed

        // this bundles required jars - kotlin-stdlib etc.
        configurations["compileClasspath"].forEach { file: File ->
//            println(file.absoluteFile)
            from(zipTree(file.absoluteFile))
        }

    }
}

