plugins {
    id("java")
    id("idea")
    id("eclipse")
}

repositories {
    mavenCentral()
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.eclipse.jetty:jetty-server:9.3.+")
    implementation("org.eclipse.jetty:jetty-client:9.3.+")
    implementation("org.eclipse.jetty.http2:http2-server:9.3.+")
    implementation("org.eclipse.jetty.http2:http2-client:9.3.+")
    implementation("org.eclipse.jetty.http2:http2-http-client-transport:9.3.+")
}

tasks {
    register("server", JavaExec::class.java) {
        dependsOn(withType<JavaCompile>())
        classpath = sourceSets["main"].runtimeClasspath
        main = "test.jetty.TestServer"
    }

    register("client", JavaExec::class.java) {
        dependsOn(withType<JavaCompile>())
        classpath = sourceSets["main"].runtimeClasspath
        main = "test.jetty.TestClient"
    }

    // To update, run: ./gradlew wrapper --gradle-version X.Y.Z
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }
}
