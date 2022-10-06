package com.biz.eulermoters.presentation.fragments.BaseFragment

import android.Manifest
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.biz.eulermoters.domain.util.StorageUtil
import com.google.android.material.transition.MaterialSharedAxis
import java.lang.reflect.ParameterizedType
import javax.inject.Inject


abstract class BaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {

    protected lateinit var binding: VB private set
    protected lateinit var viewModel: VM private set

    private val type = (javaClass.genericSuperclass as ParameterizedType)
    private val classVB = type.actualTypeArguments[0] as Class<VB>
    private val classVM = type.actualTypeArguments[1] as Class<VM>

    var check : MutableLiveData<Boolean> = MutableLiveData()

    @Inject lateinit var storageUtil: StorageUtil


    private var _binding : ViewBinding? = null
        get() = binding

    private val inflateMethod = classVB.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        permissions.entries.forEach {
            Log.e("DEBUG", "${it.key} = ${it.value}")
            if (it.value == true){
                check.value = true
            }else{
                return@forEach
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionCheck()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = inflateMethod.invoke(null, inflater, container, false) as VB
        viewModel = createViewModelLazy(classVM.kotlin, { viewModelStore }).value

        return binding.root
    }

    private val clickTag = "__click__"
    fun View.singleClickListener(debounceTime: Long = 1200L, action: () -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime: Long = 0
            override fun onClick(v: View) {
                val timeNow = SystemClock.elapsedRealtime()
                val elapsedTimeSinceLastClick = timeNow - lastClickTime
                showlog( """
                        DebounceTime: $debounceTime
                        Time Elapsed: $elapsedTimeSinceLastClick
                        Is within debounce time: ${elapsedTimeSinceLastClick < debounceTime}
                    """.trimIndent())

                if (elapsedTimeSinceLastClick < debounceTime) {
                    showlog("Double click shielded")
                    return
                } else {
                    showlog("Click happened")
                    action()
                }
                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }

    fun showtoast(str : String) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show()
    }

    fun showlog(str : String)  {
        Log.d("LogTag", str)
    }

    private fun PermissionCheck() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}