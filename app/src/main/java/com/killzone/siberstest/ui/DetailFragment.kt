package com.killzone.siberstest.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.killzone.siberstest.R
import com.killzone.siberstest.databinding.FragmentDetailBinding
import com.killzone.siberstest.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class DetailFragment : Fragment() {

    private val navArgs: DetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentDetailBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)

        Glide.with(this)
            .applyDefaultRequestOptions(requestOptions)
            .load(navArgs.Pokemon.imageUrl)
            .onlyRetrieveFromCache(true)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.imageView)

        binding.txtType.text = "Type: ${navArgs.Pokemon.type.toString()}"
        binding.txtAttack.text = "Attack value: ${navArgs.Pokemon.attack.toString()}"
        binding.txtName.text = "${navArgs.Pokemon.name.toString()}"
        binding.txtDefense.text = "Defense value: ${navArgs.Pokemon.defense.toString()}"
        binding.txtHp.text = "Hp value: ${navArgs.Pokemon.hp.toString()}"

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }


}