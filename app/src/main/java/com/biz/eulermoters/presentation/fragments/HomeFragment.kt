package com.biz.eulermoters.presentation.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log


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
        ".mp3"
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
//           setupVideoView(mediaController)
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
        val str = getExternalMounts()
        Log.d("LogTag", str.toString())

        if (str != null) {
            for (path in str) {
                val result: String = path.substring(path.lastIndexOf('/') + 1).trim()
                storage = File(Environment.getExternalStorageState())
                Log.d("LogTag", storage?.path.toString())
                load_Directory_Files(storage!!)
            }
        }

//        storagePaths = storageUtil.getStorageDirectories(requireActivity().applicationContext)
//
//        for (path in storagePaths) {
//            storage = File(path)
//            Log.d("LogTagStorage", path.toString())
//            load_Directory_Files(storage!!)
//        }

        withContext(Dispatchers.Main){
            recyclerViewAdapter.notifyDataSetChanged()
            setupMediaPlayer()
        }
    }




    fun getExternalMounts(): kotlin.collections.ArrayList<String>? {
        val out = ArrayList<String>()
        val reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*"
        var s = ""
        try {
            val process = ProcessBuilder().command("mount").redirectErrorStream(true).start()
            process.waitFor()
            val `is`: InputStream = process.inputStream
            val buffer = ByteArray(1024)
            while (`is`.read(buffer) !== -1) {
                s = s + String(buffer)
            }
            `is`.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        // parse output
        val lines = s.split("\n").toTypedArray()
        for (line in lines) {
            if (!line.lowercase(Locale.US).contains("asec")) {
                if (line.matches(reg.toRegex())) {
                    val parts = line.split(" ").toTypedArray()
                    for (part in parts) {
                        if (part.startsWith("/")) if (!part.lowercase(Locale.US).contains("vold")
                        ) out.add(part)
                    }
                }
            }
        }
        return out
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
        if (fileList != null) {
            Log.d("LogTag", fileList.toString())
        }
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