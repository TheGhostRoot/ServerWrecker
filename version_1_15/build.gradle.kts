plugins {
    id("net.pistonmaster.java-conventions")
}

dependencies {
    implementation("com.github.GeyserMC:MCProtocolLib:1.15.2-1")
    compileOnly(projects.serverwreckerCommon)
}