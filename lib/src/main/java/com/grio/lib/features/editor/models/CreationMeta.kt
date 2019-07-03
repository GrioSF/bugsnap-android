package com.grio.lib.features.editor.models

import com.google.gson.annotations.SerializedName

data class CreationMeta(
    @SerializedName("expand")
    val expand: String?,
    @SerializedName("projects")
    val projects: List<Project>

)

data class Project(
    @SerializedName("self")
    val self: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("key")
    val key: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatarUrls")
    val avatarUrls: Map<String, String>,
    @SerializedName("issuetypes")
    val issueTypes: List<IssueType>
)

data class IssueType(
    @SerializedName("self")
    val self: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("iconUrl")
    val iconUrl: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("subtask")
    val subtask: Boolean
)
