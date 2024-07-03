import com.kobylynskyi.graphql.codegen.model.GeneratedLanguage
import io.github.kobylynskyi.graphql.codegen.gradle.GraphQLCodegenGradleTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.kotlin)
    alias(libs.plugins.graphql.codegen)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(rootProject.libs.spring.boot.dependencies))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
}

kotlin {
    jvmToolchain(21)
}

tasks.named<GraphQLCodegenGradleTask>("graphqlCodegen") {
    graphqlSchemaPaths = file("$projectDir/src/main/resources/graphql/").listFiles()!!.map { it.path }
    outputDir = layout.buildDirectory.file("generated").get().asFile
    packageName = "graphql.model"
    generateBuilder = false
    generateApis = false
    generateImmutableModels = true
    modelValidationAnnotation = "@org.jetbrains.annotations.NotNull"
    generatedLanguage = GeneratedLanguage.KOTLIN
    isGenerateModelOpenClasses = false
    isInitializeNullableTypes = true
}

sourceSets {
    getByName("main").java.srcDirs(layout.buildDirectory.file("generated/graphql").get().asFile)
}

tasks.named<KotlinCompile>("compileKotlin") {
    dependsOn("graphqlCodegen")
}

// TODO log a bug that without this the schema.graphqls in resources is not found on the classpath
tasks.named<BootRun>("bootRun") {
    sourceResources(sourceSets["main"])
}
