package com.kemarport.kyms.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.kemarport.kyms.R
import com.kemarport.kyms.databinding.FragmentSupervisorMainOptionsBinding


class SupervisorMainOptionsFragment : Fragment(), View.OnClickListener  {

    lateinit var binding:FragmentSupervisorMainOptionsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_supervisor_main_options, container, false)
        return binding.root
    }

    override fun onClick(p0: View?) {
        p0?.let {
            when (it.id) {
                binding.mcvSupervisorImport .id -> {
                   /* val action =
                        SupervisorMainOptionsFragmentDirections.actionSupervisorMainOptionsFragmentToSupervisorFragment2()
                    findNavController().navigate(action)*/
                }
                binding.mcvSupervisorExport .id -> {
                    val action =
                        SupervisorMainOptionsFragmentDirections.actionSupervisorMainOptionsFragmentToSupervisorFragment2()
                    findNavController().navigate(action)
                }
            }
        }
    }
}