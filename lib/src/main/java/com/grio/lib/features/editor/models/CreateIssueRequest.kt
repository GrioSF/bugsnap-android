package com.grio.lib.features.editor.models

import com.google.gson.annotations.SerializedName


data class CreateIssueRequest(
    @SerializedName("fields")
    var fields: CreateIssueFields

) {
    data class CreateIssueFields(
        @SerializedName("project")
        var project: CreateIssueProject,
        @SerializedName("summary")
        var summary: String,
        @SerializedName("description")
        var descrption: String,
        @SerializedName("issuetype")
        var issueType: CreateIssueType
    )

    data class CreateIssueProject(
        @SerializedName("key")
        var key: String
    )

    data class CreateIssueType(
        @SerializedName("id")
        var id: Int
    )

    companion object {
        @JvmStatic
        fun create(project: String, summary: String, description: String, id: Int): CreateIssueRequest {
            return CreateIssueRequest(
                CreateIssueFields(
                    project = CreateIssueProject(project),
                    summary = summary,
                    descrption = description,
                    issueType = CreateIssueType(id)
                )
            )
        }
    }
}



