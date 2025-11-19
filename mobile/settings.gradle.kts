// ✅ Quản lý plugin cho toàn bộ project
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// ✅ Cấu hình nơi tìm dependencies cho tất cả module (app, lib,…)
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// ✅ Đặt tên root project & include module app
rootProject.name = "NutriCook"
include(":app")
