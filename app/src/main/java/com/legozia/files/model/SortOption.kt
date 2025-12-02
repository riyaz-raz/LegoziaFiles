package com.legozia.files.model

enum class SortType {
    NAME,
    SIZE,
    DATE,
    TYPE
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

data class SortOption(
    val type: SortType = SortType.NAME,
    val direction: SortDirection = SortDirection.ASCENDING
) {
    fun getDisplayName(): String {
        val typeName = when (type) {
            SortType.NAME -> "Name"
            SortType.SIZE -> "Size"
            SortType.DATE -> "Date"
            SortType.TYPE -> "Type"
        }
        val directionSymbol = when (direction) {
            SortDirection.ASCENDING -> "↑"
            SortDirection.DESCENDING -> "↓"
        }
        return "$typeName $directionSymbol"
    }
}
