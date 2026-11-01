plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.theschoolsecond_handtrade"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.theschoolsecond_handtrade"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // ===== 本项目全部使用 Java + XML 布局，不需要 Compose / Kotlin =====
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.androidx.recyclerview)        // 首页商品列表
    implementation(libs.androidx.swiperefreshlayout)  // 下拉刷新
    implementation(libs.glide)                        // 图片加载

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
