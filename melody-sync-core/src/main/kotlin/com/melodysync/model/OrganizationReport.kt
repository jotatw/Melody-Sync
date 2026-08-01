package com.melodysync.model

import java.nio.file.Path

data class PlannedMove(
    val song: Song,
    val from: Path,
    val to: Path,
    val reason: String,
)

data class OrganizationReport(
    val directory: Path,
    val plannedMoves: List<PlannedMove>,
    val moved: Int,
    val skipped: Int,
    val errors: List<String>,
) {
    val alreadyOrganized: Int
        get() = plannedMoves.count { it.from == it.to }

    val toMove: Int
        get() = plannedMoves.size - alreadyOrganized
}
