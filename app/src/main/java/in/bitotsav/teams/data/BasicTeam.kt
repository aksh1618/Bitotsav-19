package `in`.bitotsav.teams.data

import android.app.AlertDialog
import android.content.Context

class BasicTeam(
    private val teamName: String,
    private val memberNames: Array<String>
) {
    fun showDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(teamName)
            .setItems(memberNames, null)
            .setPositiveButton("Back", null)
            .create()
            .show()
    }
}
