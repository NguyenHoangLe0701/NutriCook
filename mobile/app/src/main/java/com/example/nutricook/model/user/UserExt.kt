package com.example.nutricook.model.user

fun IUser.asUser(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl
)

fun User.bestName(): String =
    (displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")).trim()

fun User.initial(): String =
    bestName().firstOrNull()?.uppercase() ?: "N"

fun User.maskedEmail(): String {
    val at = email.indexOf('@')
    if (at <= 1) return email
    val name = email.substring(0, at)
    val domain = email.substring(at)
    return name.first() + "***" + domain
}
