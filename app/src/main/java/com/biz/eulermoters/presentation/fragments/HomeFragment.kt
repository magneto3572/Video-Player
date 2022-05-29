package com.biz.eulermoters.presentation.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.biz.eulermoters.data.recyclerAdapter
import com.biz.eulermoters.databinding.FragmentHomeBinding
import com.biz.eulermoters.presentation.fragments.BaseFragment.BaseFragment
import com.biz.eulermoters.presentation.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(), recyclerAdapter.OnClickInterface {

    private lateinit var recyclerViewAdapter : recyclerAdapter
    private lateinit var mediaController : MediaController
    private var storage: File? = null
    lateinit var storagePaths: Array<String>
    private var allMediaList = ArrayList<File>()
    private var mLayoutManager : LinearLayoutManager? = null
    private var currentVideo : Int = 0

    var videoExtensions = arrayOf(
        ".mp4", ".ts", ".mkv", ".mov",
        ".3gp", ".mv2", ".m4v", ".webm", ".mpeg1", ".mpeg2", ".mts", ".ogm",
        ".bup", ".dv", ".flv", ".m1v", ".m2ts", ".mpeg4", ".vlc", ".3g2",
        ".avi", ".mpeg", ".mpg", ".wmv", ".asf"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaController = MediaController(requireContext())
        setupRecyclerView()
        setupFiles()
    }


    private fun setupMediaPlayer(){
        binding.apply {
            mediaController.setAnchorView(videoView)
            mediaController.setPrevNextListeners({
                nextVideo()
            }, {
                prevVideo()
            })
           setupVideoView(mediaController)
        }
    }

    private fun nextVideo(){
        if (currentVideo != allMediaList.size-1){
            if (currentVideo>0){
                currentVideo++
                setupVideoView(mediaController)
            }
        }else{
            showtoast("list is empty")
        }
    }

    private fun prevVideo(){
        if (currentVideo>0){
            currentVideo--
            setupVideoView(mediaController)
        }else{
            showtoast("This is the first video in the list")
        }
    }

    private fun setupVideoView(mediaController: MediaController){
        binding.apply {
            val uri = Uri.fromFile(allMediaList[currentVideo])
            videoView.setMediaController(mediaController)
            videoView.setVideoURI(uri)
            videoView.requestFocus()
            videoView.setOnCompletionListener {
                videoView.stopPlayback()
            }
            videoView.start()
        }
    }


    private fun setupFiles(){
        check.observe(viewLifecycleOwner){
            if (it == true){
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                lifecycleScope.launch(Dispatchers.IO){
                    loadFiles()
                }
            }else{
                showtoast("Please Allow All Permission")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        binding.videoView.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            videoView.stopPlayback()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadFiles(){
        storagePaths = storageUtil.getStorageDirectories(requireActivity().applicationContext)
        for (path in storagePaths) {
            storage = File(path)
            load_Directory_Files(storage!!)
        }
        withContext(Dispatchers.Main){
            recyclerViewAdapter.notifyDataSetChanged()
            setupMediaPlayer()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            mLayoutManager = LinearLayoutManager(requireContext())
            layoutManager = mLayoutManager
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isNestedScrollingEnabled = false
            recyclerViewAdapter = recyclerAdapter(requireContext(), allMediaList, this@HomeFragment)
            adapter = recyclerViewAdapter
        }
    }

    suspend fun load_Directory_Files(directory: File) {
        val fileList = directory.listFiles()
        try {
            if (fileList != null && fileList.isNotEmpty()) {
                for (i in fileList.indices) {
                    if (fileList[i].isDirectory) {
                        load_Directory_Files(fileList[i])
                    } else {
                        val name = fileList[i].name.lowercase(Locale.getDefault())
                        for (extension in videoExtensions) {
                            if (name.endsWith(extension)) {
                                allMediaList.add(fileList[i])
                                withContext(Dispatchers.Main){
                                    binding.progressBar.visibility = View.GONE
                                    binding.recyclerView.visibility = View.VISIBLE
                                }
                                break
                            }
                        }
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onitemClick(pos: Int) {
        currentVideo = pos
        setupVideoView(mediaController)
    }
}