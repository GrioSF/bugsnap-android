package com.grio.lib.features.reporter.models

import com.google.gson.annotations.SerializedName

data class CreateIssueResponse(
    @SerializedName("id")
    var id: String,
    @SerializedName("key")
    var key: String,
    @SerializedName("self")
    var self: String
)