package com.hjy.bsdiff

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.hjy.bsdiff.databinding.FragmentFirstBinding
import java.io.File

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            activity?.let {
                var oldFile = File(it.cacheDir, "old.bundle")
                var newFile = File(it.cacheDir, "new.bundle")
                var patchFile = File(it.cacheDir, "patch.bundle")
                if (!oldFile.exists() || !newFile.exists()) {
                    Toast.makeText(it, "请在 cache 目录中先放入 old.bundle、new.bundle 包", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                var result = FileDiffer.fileDiff(oldFile.absolutePath, newFile.absolutePath, patchFile.absolutePath)
                Toast.makeText(it, "生成差分包: result = $result", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonCombine.setOnClickListener {
            activity?.let {
                var oldFile = File(it.cacheDir, "old.bundle")
                var newFile = File(it.cacheDir, "combine.bundle")
                var patchFile = File(it.cacheDir, "patch.bundle")
                if (!oldFile.exists()) {
                    Toast.makeText(it, "请在 cache 目录中先放入 old.bundle、new.bundle 包", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (!patchFile.exists()) {
                    Toast.makeText(it, "请先生成 patch.bundle 差分包", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                var result = FileDiffer.fileCombine(oldFile.absolutePath, newFile.absolutePath, patchFile.absolutePath)
                Toast.makeText(it, "合并包: result = $result", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}