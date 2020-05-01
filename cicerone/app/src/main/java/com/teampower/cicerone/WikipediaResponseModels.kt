package com.teampower.cicerone

data class WikipediaSearchResponse(
    val query: WikipediaQueryResponse
)

class WikipediaQueryResponse(
    val search: List<WikipediaSearchQueryPageItem>
)

data class WikipediaSearchQueryPageItem(
    val title: String,
    val pageid: Int,
    val snippet: String
)