package `in`.bitotsav.teams.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentLeaderboardBinding
import `in`.bitotsav.databinding.ItemTeamBinding
import `in`.bitotsav.shared.ui.BaseFragment
import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter
import `in`.bitotsav.shared.utils.executeAfter
import `in`.bitotsav.shared.utils.getColorCompat
import `in`.bitotsav.shared.utils.setObserver
import `in`.bitotsav.teams.championship.data.ChampionshipTeam
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.viewModel


class LeaderboardFragment : BaseFragment() {

    private val leaderboardViewModel by viewModel<LeaderboardViewModel>()

    private val adapter by lazy {
        SimpleRecyclerViewAdapter<ChampionshipTeam>(
            { inflater, parent, bool ->
                ItemTeamBinding.inflate(inflater, parent, bool)
            },
            { itemBinding, teamItem ->
                (itemBinding as ItemTeamBinding).executeAfter {
                    this.rank = teamItem.rank.toString()
                    this.name = teamItem.name
                    this.points = teamItem.totalScore.toString()
                    this.color = itemBinding.nameView.context.getColorCompat(R.color.textColor)
                    this.listener = getTeamItemListener(teamItem)
                    lifecycleOwner = this@LeaderboardFragment
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        leaderboardViewModel.mColor = TypedValue().apply {
            activity?.theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data

        return FragmentLeaderboardBinding.inflate(inflater, container, false)
            .apply {
                viewModel = leaderboardViewModel
                teams.adapter = adapter.apply {
                    submitList(leaderboardViewModel.teams.value)
                }
                lifecycleOwner = viewLifecycleOwner
                setObservers()
                searchFab.setOnClickListener {
                    teamSearch.addTextChangedListener {
                        applyFilter(it.toString())
                    }
                }
            }
            .root
    }

    private fun applyFilter(query: String) {
        adapter.submitList(
            leaderboardViewModel.teams.value?.filter {
                it.name.contains(query, ignoreCase = true)
            }
        )
    }

    private fun setObservers() {
        leaderboardViewModel.teams.setObserver(viewLifecycleOwner) { teams ->
            with(adapter) {
                submitList(teams)
                notifyDataSetChanged()
            }
        }
    }


    private fun getTeamItemListener(teamItem: ChampionshipTeam) = View.OnClickListener {
    }

}
