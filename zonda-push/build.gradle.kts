plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = "com.zonda.push"
version = "1.0.0"

android {
    namespace = "com.zonda.push"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = false
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.work:work-runtime:2.10.0")

    testImplementation("junit:junit:4.13.2")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.zonda.push"
                artifactId = "push-sdk"
                version = project.version.toString()
                pom {
                    name.set("Zonda Push SDK")
                    description.set("Android push SDK. Each app uses its own zp_ key.")
                    url.set("https://github.com/zondaleadapps/push-sdk")
                }
            }
        }
        repositories {
            val token = providers.environmentVariable("GITHUB_TOKEN")
                .orElse(providers.gradleProperty("githubMavenToken"))
                .orNull
            if (!token.isNullOrBlank()) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/zondaleadapps/push-sdk")
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR")
                            .orElse(providers.gradleProperty("gpr.user"))
                            .orElse("x-access-token")
                            .get()
                        password = token
                    }
                }
            }
        }
    }
}
