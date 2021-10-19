/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.recyclerview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.window.java.layout.WindowInfoRepositoryCallbackAdapter
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.recyclerview.FoldableStaggeredItemDecoration
import com.microsoft.device.dualscreen.recyclerview.FoldableStaggeredLayoutManager
import com.microsoft.device.dualscreen.recyclerview.utils.replaceItemDecorationAt
import com.microsoft.device.dualscreen.sample_duolayoutmanager.R
import com.microsoft.device.dualscreen.sample_duolayoutmanager.databinding.ActivityMainBinding
import com.microsoft.device.surfaceduo.recyclerview.utils.NumbersStaggeredAdapter
import java.util.concurrent.Executor

class StaggeredActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: WindowInfoRepositoryCallbackAdapter
    private lateinit var consumerWindowLayoutInfo: Consumer<WindowLayoutInfo>
    private lateinit var runOnUiThreadExecutor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        initWindowLayoutInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_staggered, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main_grid -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.hasFixedSize()
        binding.recyclerView.adapter = NumbersStaggeredAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun initWindowLayoutInfo() {
        adapter = WindowInfoRepositoryCallbackAdapter(windowInfoRepository())
        runOnUiThreadExecutor = Executor { command: Runnable? ->
            command?.let {
                Handler(Looper.getMainLooper()).post(it)
            }
        }
        consumerWindowLayoutInfo = Consumer { windowLayoutInfo ->
            onWindowLayoutInfoChanged(windowLayoutInfo)
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.addWindowLayoutInfoListener(runOnUiThreadExecutor, consumerWindowLayoutInfo)
    }

    override fun onStop() {
        super.onStop()
        adapter.removeWindowLayoutInfoListener(consumerWindowLayoutInfo)
    }

    private fun onWindowLayoutInfoChanged(windowLayoutInfo: WindowLayoutInfo) {
        binding.recyclerView.layoutManager = FoldableStaggeredLayoutManager(this, windowLayoutInfo).get()
        binding.recyclerView.replaceItemDecorationAt(FoldableStaggeredItemDecoration(windowLayoutInfo))
    }
}