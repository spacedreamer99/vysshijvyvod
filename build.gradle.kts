plugins {
    java
    application
}

group = "org.narrativ27"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.joml:joml:1.4.0")
    implementation("org.lwjgl:lwjgl-stb")
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-linux")
    implementation(platform("org.lwjgl:lwjgl-bom:3.4.1"))
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-openal")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-linux")
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-linux")
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = "natives-linux")
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = "natives-linux")
    
    implementation("org.lwjgl:lwjgl-assimp")
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = "natives-linux")
    implementation("net.onedaybeard.artemis:artemis-odb:2.3.0")
    
}

application {
    mainClass.set("org.narrativ27.vysshijvyvod.Main")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}