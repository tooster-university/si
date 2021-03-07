import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
}

group = "me.tooster"
version = "w1z1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "15"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "me.tooster.w1.Z1"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }

}

//val solutions = File("src/main/kotlin/me/tooster").walk()
//    .mapNotNull { Regex(""".*(w\d+)/(z\d+.*)\.kt""").matchEntire(it.path) }
//
//solutions.forEach {
//    val (w, z) = it.groupValues[1] to it.groupValues[2]
//    tasks.register<CreateStartScripts>("${w}${z}") {
//        group = "build solutions"
//        outputDir = File("build/out/$w")
//        mainClassName = "me.tooster.w1.Z2_polishWordsKt"
//        applicationName = z
//        classpath = tasks.getByName("jar").outputs.files + configurations.runtimeClasspath.get()
//    }
//}
//
