package com.example.dartscore.game

import com.example.dartscore.model.*

object LocalGameEngine {

    fun legStarterIndex(leg: Int, playerCount: Int): Int = (leg - 1) % playerCount

    fun createInitialState(settings: MatchSettings, matchId: String? = null): LocalGameState {
        val players = settings.playerNames.map { name ->
            PlayerGameState(
                name = name,
                remaining = settings.startScore,
                hasOpened = settings.inRule == InRule.STRAIGHT
            )
        }
        return LocalGameState(
            settings = settings,
            players = players,
            matchId = matchId,
            lastTurnStartRemaining = players.first().remaining
        )
    }

    fun submitVisit(state: LocalGameState, score: Int): VisitResult {
        if (state.isFinished) return VisitResult(state, VisitOutcome.INVALID)

        val playerIndex = state.currentPlayerIndex
        val player = state.currentPlayer
        val remainingBefore = player.remaining

        if (score < 0 || score > 180) {
            return VisitResult(state, VisitOutcome.INVALID_SCORE)
        }

        // Zero score = empty visit, advance turn
        if (score == 0) {
            val visit = VisitRecord(
                playerIndex = playerIndex,
                playerName = player.name,
                score = 0,
                remainingBefore = remainingBefore,
                remainingAfter = remainingBefore,
                leg = state.currentLeg,
                set = state.currentSet
            )
            return VisitResult(
                advanceTurn(state, visit),
                VisitOutcome.SCORED
            )
        }

        if (!player.hasOpened && settingsRequireOpen(state.settings.inRule)) {
            if (!isValidOpeningScore(score, state.settings.inRule)) {
                val visit = VisitRecord(
                    playerIndex = playerIndex,
                    playerName = player.name,
                    score = 0,
                    remainingBefore = remainingBefore,
                    remainingAfter = remainingBefore,
                    leg = state.currentLeg,
                    set = state.currentSet
                )
                return VisitResult(
                    advanceTurn(state, visit),
                    VisitOutcome.NO_OPEN
                )
            }
        }

        val newRemaining = remainingBefore - score

        if (newRemaining < 0) {
            val visit = VisitRecord(
                playerIndex = playerIndex,
                playerName = player.name,
                score = score,
                remainingBefore = remainingBefore,
                remainingAfter = remainingBefore,
                leg = state.currentLeg,
                set = state.currentSet,
                bust = true
            )
            return VisitResult(bustTurn(state, visit), VisitOutcome.BUST)
        }

        if (state.settings.outRule == OutRule.DOUBLE && newRemaining == 1) {
            val visit = VisitRecord(
                playerIndex = playerIndex,
                playerName = player.name,
                score = score,
                remainingBefore = remainingBefore,
                remainingAfter = remainingBefore,
                leg = state.currentLeg,
                set = state.currentSet,
                bust = true
            )
            return VisitResult(bustTurn(state, visit), VisitOutcome.BUST)
        }

        if (newRemaining == 0) {
            if (!CheckoutChart.isValidFinish(remainingBefore, state.settings.outRule)) {
                val visit = VisitRecord(
                    playerIndex = playerIndex,
                    playerName = player.name,
                    score = score,
                    remainingBefore = remainingBefore,
                    remainingAfter = remainingBefore,
                    leg = state.currentLeg,
                    set = state.currentSet,
                    bust = true
                )
                return VisitResult(bustTurn(state, visit), VisitOutcome.BUST)
            }

            return VisitResult(
                state = state,
                outcome = VisitOutcome.CHECKOUT_PENDING,
                pendingCheckoutScore = score
            )
        }

        val opened = player.hasOpened ||
            state.settings.inRule == InRule.STRAIGHT ||
            isValidOpeningScore(score, state.settings.inRule)

        val updatedPlayer = applyVisit(player, score, newRemaining, opened)
        val updatedPlayers = state.players.toMutableList().apply {
            this[playerIndex] = updatedPlayer
        }
        val visit = VisitRecord(
            playerIndex = playerIndex,
            playerName = player.name,
            score = score,
            remainingBefore = remainingBefore,
            remainingAfter = newRemaining,
            leg = state.currentLeg,
            set = state.currentSet
        )
        return VisitResult(
            advanceTurn(state.copy(players = updatedPlayers), visit),
            VisitOutcome.SCORED
        )
    }

    fun confirmCheckout(state: LocalGameState, score: Int, dartsUsed: Int): VisitResult {
        if (state.isFinished) return VisitResult(state, VisitOutcome.INVALID)

        val playerIndex = state.currentPlayerIndex
        val player = state.currentPlayer
        val remainingBefore = player.remaining

        if (remainingBefore - score != 0 ||
            !CheckoutChart.isValidFinish(remainingBefore, state.settings.outRule)
        ) {
            return VisitResult(state, VisitOutcome.INVALID)
        }

        if (!CheckoutDartOptions.isValid(dartsUsed, state.settings.outRule)) {
            return VisitResult(state, VisitOutcome.INVALID)
        }

        val updatedPlayer = applyVisit(player, score, 0, opened = true)
        val updatedPlayers = state.players.toMutableList().apply {
            this[playerIndex] = updatedPlayer
        }
        val visit = VisitRecord(
            playerIndex = playerIndex,
            playerName = player.name,
            score = score,
            remainingBefore = remainingBefore,
            remainingAfter = 0,
            leg = state.currentLeg,
            set = state.currentSet,
            legWon = true,
            dartsUsed = dartsUsed
        )
        val afterLeg = handleLegWin(
            state.copy(players = updatedPlayers, matchVisits = state.matchVisits + visit),
            playerIndex,
            visit
        )
        return VisitResult(afterLeg, VisitOutcome.LEG_WON, visit = visit)
    }

    fun bust(state: LocalGameState): LocalGameState {
        val player = state.currentPlayer
        val visit = VisitRecord(
            playerIndex = state.currentPlayerIndex,
            playerName = player.name,
            score = 0,
            remainingBefore = player.remaining,
            remainingAfter = player.remaining,
            leg = state.currentLeg,
            set = state.currentSet,
            bust = true
        )
        return bustTurn(state, visit)
    }

    fun undoLastVisit(state: LocalGameState): LocalGameState {
        if (state.isFinished || state.matchVisits.isEmpty()) return state

        val lastVisit = state.matchVisits.last()
        val updatedVisits = state.matchVisits.dropLast(1)
        val playerIndex = lastVisit.playerIndex
        val player = state.players[playerIndex]

        val restoredHistory = when {
            lastVisit.bust -> player.visitHistory.dropLast(1)
            lastVisit.score == 0 -> player.visitHistory
            else -> player.visitHistory.dropLast(1)
        }

        val restoredRemaining = lastVisit.remainingBefore
        val restoredPlayer = player.copy(
            remaining = restoredRemaining,
            visitHistory = restoredHistory,
            matchVisitCount = if (lastVisit.bust || lastVisit.score == 0) {
                player.matchVisitCount
            } else {
                (player.matchVisitCount - 1).coerceAtLeast(0)
            },
            matchTotalScore = if (lastVisit.bust || lastVisit.score == 0) {
                player.matchTotalScore
            } else {
                (player.matchTotalScore - lastVisit.score).coerceAtLeast(0)
            },
            hasOpened = if (restoredHistory.isEmpty()) {
                state.settings.inRule == InRule.STRAIGHT
            } else {
                player.hasOpened
            },
            legsWon = if (lastVisit.legWon) (player.legsWon - 1).coerceAtLeast(0) else player.legsWon
        )

        val updatedPlayers = state.players.toMutableList().apply {
            this[playerIndex] = restoredPlayer
        }

        return state.copy(
            players = updatedPlayers,
            currentPlayerIndex = playerIndex,
            matchVisits = updatedVisits,
            lastVisitPlayerIndex = updatedVisits.lastOrNull()?.playerIndex,
            lastTurnStartRemaining = restoredRemaining,
            isFinished = false,
            winnerIndex = null
        )
    }

    private fun applyVisit(
        player: PlayerGameState,
        score: Int,
        newRemaining: Int,
        opened: Boolean
    ): PlayerGameState {
        return player.copy(
            remaining = newRemaining,
            visitHistory = player.visitHistory + LegVisitEntry.Scored(score),
            hasOpened = opened || player.hasOpened,
            matchTotalScore = player.matchTotalScore + score,
            matchVisitCount = player.matchVisitCount + 1
        )
    }

    private fun bustTurn(state: LocalGameState, visit: VisitRecord): LocalGameState {
        val player = state.currentPlayer
        val resetRemaining = state.lastTurnStartRemaining ?: player.remaining
        val resetPlayer = player.copy(
            remaining = resetRemaining,
            visitHistory = player.visitHistory + LegVisitEntry.Bust
        )
        val updatedPlayers = state.players.toMutableList().apply {
            this[state.currentPlayerIndex] = resetPlayer
        }
        return advanceTurn(
            state.copy(
                players = updatedPlayers,
                matchVisits = state.matchVisits + visit
            ),
            null
        )
    }

    private fun advanceTurn(state: LocalGameState, visit: VisitRecord?): LocalGameState {
        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        val visits = if (visit != null) state.matchVisits + visit else state.matchVisits
        return state.copy(
            currentPlayerIndex = nextIndex,
            matchVisits = visits,
            lastVisitPlayerIndex = visit?.playerIndex ?: state.lastVisitPlayerIndex,
            lastTurnStartRemaining = state.players[nextIndex].remaining
        )
    }

    private fun handleLegWin(
        state: LocalGameState,
        winnerIndex: Int,
        visit: VisitRecord
    ): LocalGameState {
        val settings = state.settings
        val updatedPlayers = state.players.mapIndexed { index, player ->
            if (index == winnerIndex) {
                when (settings.unit) {
                    MatchUnit.LEGS -> player.copy(legsWon = player.legsWon + 1)
                    MatchUnit.SETS -> player.copy(legsInCurrentSet = player.legsInCurrentSet + 1)
                }
            } else {
                player
            }
        }.toMutableList()

        val winner = updatedPlayers[winnerIndex]
        val matchWon = when (settings.unit) {
            MatchUnit.LEGS -> hasReachedTarget(winner.legsWon, settings)
            MatchUnit.SETS -> {
                val legsToWinSet = legsNeededToWinSet(settings)
                if (winner.legsInCurrentSet >= legsToWinSet) {
                    updatedPlayers[winnerIndex] = winner.copy(
                        setsWon = winner.setsWon + 1,
                        legsInCurrentSet = 0
                    )
                    hasReachedTarget(updatedPlayers[winnerIndex].setsWon, settings)
                } else {
                    false
                }
            }
        }

        if (matchWon) {
            return state.copy(
                players = updatedPlayers,
                isFinished = true,
                winnerIndex = winnerIndex
            )
        }

        val resetPlayers = updatedPlayers.map {
            it.copy(
                remaining = settings.startScore,
                visitHistory = emptyList(),
                hasOpened = settings.inRule == InRule.STRAIGHT
            )
        }
        val nextLeg = state.currentLeg + 1
        val nextSet = if (settings.unit == MatchUnit.SETS &&
            updatedPlayers[winnerIndex].legsInCurrentSet == 0 &&
            updatedPlayers[winnerIndex].setsWon > state.players[winnerIndex].setsWon
        ) {
            state.currentSet + 1
        } else {
            state.currentSet
        }

        val starterIndex = legStarterIndex(nextLeg, settings.playerNames.size)

        return LocalGameState(
            settings = settings,
            players = resetPlayers,
            currentPlayerIndex = starterIndex,
            currentLeg = nextLeg,
            currentSet = nextSet,
            matchVisits = state.matchVisits,
            matchId = state.matchId,
            lastVisitPlayerIndex = visit.playerIndex,
            lastTurnStartRemaining = settings.startScore
        )
    }

    private fun hasReachedTarget(won: Int, settings: MatchSettings): Boolean {
        return when (settings.format) {
            MatchFormat.FIRST_TO -> won >= settings.count
            MatchFormat.BEST_OF -> won > settings.count / 2
        }
    }

    private fun legsNeededToWinSet(settings: MatchSettings): Int {
        return when (settings.format) {
            MatchFormat.FIRST_TO -> settings.count
            MatchFormat.BEST_OF -> settings.count / 2 + 1
        }
    }

    private fun settingsRequireOpen(inRule: InRule): Boolean = inRule != InRule.STRAIGHT

    private fun isValidOpeningScore(score: Int, inRule: InRule): Boolean {
        if (score == 0) return true
        return when (inRule) {
            InRule.STRAIGHT -> true
            InRule.DOUBLE -> score >= 2 && score % 2 == 0
            InRule.MASTER -> score == 25 || score == 50 || (score >= 2 && score % 2 == 0)
        }
    }
}

enum class VisitOutcome {
    SCORED, BUST, LEG_WON, CHECKOUT_PENDING, NO_OPEN, INVALID, INVALID_SCORE
}

data class VisitResult(
    val state: LocalGameState,
    val outcome: VisitOutcome,
    val visit: VisitRecord? = null,
    val pendingCheckoutScore: Int? = null
)
