package `in`.bitotsav.teams.ui

import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.getWorkNameForTeamWorker
import `in`.bitotsav.shared.utils.scheduleUniqueWork
import `in`.bitotsav.shared.workers.TeamWorkType
import `in`.bitotsav.shared.workers.TeamWorker
import `in`.bitotsav.teams.championship.data.ChampionshipTeamRepository
import androidx.work.workDataOf

class LeaderboardViewModel(val championshipTeamRepository: ChampionshipTeamRepository) :
    BaseViewModel("LdrBrdVM") {

    val teams = championshipTeamRepository.getAll()

    init {
        scheduleUniqueWork<TeamWorker>(
            workDataOf("type" to TeamWorkType.FETCH_ALL_TEAMS.name),
            getWorkNameForTeamWorker(TeamWorkType.FETCH_ALL_TEAMS)
        )
    }

}