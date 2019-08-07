package com.grio.lib.features.reporter

import android.app.ProgressDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.grio.lib.R

import com.grio.lib.core.di.DaggerInjector
import com.grio.lib.core.extension.observe
import com.grio.lib.core.extension.viewModels
import com.grio.lib.core.platform.BaseActivity
import com.grio.lib.features.DeviceIdentifier
import com.grio.lib.features.editor.*
import com.grio.lib.features.LogSnapshotManager
import kotlinx.android.synthetic.main.a_reporter.*

import java.io.File
import javax.inject.Inject

class ReporterActivity :  BaseActivity() {

    @Inject
    lateinit var gson: Gson

    private lateinit var file: File
    private lateinit var viewModel: ReporterViewModel
    private var isVideo: Boolean = false
    private lateinit var loadingDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_reporter)
        DaggerInjector.getComponent().inject(this)

        viewModel = viewModels(viewModelFactory) {
            observe(isLoading, ::renderLoading)
        }

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.report_a_bug)


        intent.extras?.let {
            if (it.containsKey("videoFile") && it.getString("reportType") == "video") {
                isVideo = true
                file = intent.getSerializableExtra("videoFile") as File
            } else {
                isVideo = false
                file = DataHolder.toFile(this)
            }
        }

        viewModel.log = LogSnapshotManager.getLogTail()
        if (!viewModel.log.isNullOrBlank()) {
            log_attached_status.setImageDrawable(getDrawable(com.grio.lib.R.drawable.ic_check_green))
        }
        log_attached_label.setOnClickListener { previewLog() }
        log_attached_status.setOnClickListener { previewLog() }
    }


    private fun renderLoading(isLoading: Boolean?) {

        if (isLoading == null) {
            return
        }
        if (isLoading) {
            loadingDialog = ProgressDialog.show(
                this@ReporterActivity, "",
                "Loading. Please wait...", true
            )
        } else {
            if (loadingDialog != null) {
                loadingDialog.dismiss()
            }
            Toast.makeText(this, "Ticket created.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun previewLog() {
        if (viewModel.log.isNotEmpty()) {
            AlertDialog.Builder(this).apply {
                setTitle("Log Preview (Lines: ${viewModel.log.lines().size})")
                setMessage(viewModel.log)
                create().show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_reporter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.send) {
            // Send report
            val summary = summary_input.text.toString()
            val description = description_input.text.toString()
            if (summary.isNullOrBlank() || description.isNullOrBlank()) {
                Toast.makeText(this, "Inputs must not be blank.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.sendReportClicked(summary = summary,
                    description = description,
                    file = file,
                    logString = viewModel.log,
                    isVideo = this.isVideo,
                    deviceInformation = DeviceIdentifier.getDeviceInformation(this))
            }

            return true
        }
        return super.onOptionsItemSelected(item)
    }
}