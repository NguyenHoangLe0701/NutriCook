package com.example.nutricook.model.profile

import com.example.nutricook.model.user.IUser
import com.example.nutricook.model.user.User

private fun IUser.toUser(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl
)

object ProfileMapper {
    fun fromIUser(
        src: IUser,
        posts: Int = 0,
        following: Int = 0,
        followers: Int = 0,
        bio: String? = null,
        dayOfBirth: String? = null,
        gender: String? = null
    ): Profile = Profile(
        user = src.toUser(),
        posts = posts,
        following = following,
        followers = followers,
        bio = bio,
        dayOfBirth = dayOfBirth,
        gender = gender
    )
}
