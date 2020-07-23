package com.killzone.siberstest.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.killzone.siberstest.R
import com.killzone.siberstest.adapters.PokemonListAdapter
import com.killzone.siberstest.adapters.PokemonListListener
import com.killzone.siberstest.databinding.FragmentMainBinding
import com.killzone.siberstest.ui.viewmodels.MainViewModel
import com.killzone.siberstest.util.Constants.CONTINUOUS_LOAD_STATE
import com.killzone.siberstest.util.SpinnerState
import com.killzone.siberstest.util.UserChoice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledItems: Int = 0

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: PokemonListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        layoutManager = LinearLayoutManager(this.context)
        adapter = PokemonListAdapter(PokemonListListener { viewModel.onPokemonClicked(it) })
        adapter.submitList(viewModel.pokemonInfoList.value)

        binding.rvPokemonList.adapter = adapter
        binding.rvPokemonList.layoutManager = layoutManager

        // subscribes LiveData inside ViewModel
        subscribeUi()

        // Adds Scroll Listener to RecyclerView
        setupRecyclerView()

        // Handle the states of CheckBoxes and Button for random pokemon page
        handleCheckBoxesAndButton()

        return binding.root
    }


    private fun subscribeUi() {

        // Updates RecyclerView list when needed
        viewModel.isListChanged.observe(viewLifecycleOwner, Observer {
            if (it) {
                adapter.submitList(viewModel.pokemonInfoList.value)
                adapter.notifyDataSetChanged()
                viewModel.resetListChangedValue()
            }
        })

        // Displays error message and asks ViewModel to get cached data
        viewModel.isErrorMessage.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val message = viewModel.isErrorMessage.value
                Toast.makeText(this.context, message, Toast.LENGTH_LONG).show()
                viewModel.getCache()
            }
        })


        // Passes certain Pokemon object to DetailFragment with SafeArgs and Navigation Action
        viewModel.navigateToDetailFragment.observe(viewLifecycleOwner, Observer {
            if (it) {
                if (viewModel.pokemonToPass != null) {
                    val action = MainFragmentDirections.actionMainFragmentToDetailFragment(viewModel.pokemonToPass!!)
                    viewModel.resetNavigateValue()
                    findNavController().navigate(action)
                }
            }
        })


        // Shows either one or another spinner when value is changed/
        viewModel.loadSpinner.observe(viewLifecycleOwner, Observer {
            when (it) {
                SpinnerState.INIT_SPINNER -> binding.pbInitialLoad.visibility = View.VISIBLE
                SpinnerState.LOAD_SPINNER -> binding.pbLoad.visibility = View.VISIBLE
                SpinnerState.INVISIBLE -> {
                    binding.pbLoad.visibility = View.GONE
                    binding.pbInitialLoad.visibility = View.GONE
                } }
        })

        // Scrolls to the top of screen
        viewModel.isNeedToScroll.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.rvPokemonList.smoothScrollToPosition(0)
                viewModel.resetScrollValue()
            }
        })

    }

    private fun setupRecyclerView() {
        binding.rvPokemonList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItems = layoutManager.childCount
                totalItems = layoutManager.itemCount
                scrolledItems = layoutManager.findFirstVisibleItemPosition()

                if (isScrolling && (currentItems + scrolledItems == totalItems)) {
                    isScrolling = false
                    viewModel.loadData(CONTINUOUS_LOAD_STATE)
                }
            }
        })
    }

    private fun handleCheckBoxesAndButton() {
        binding.cbAttack.setOnClickListener {
            handleUserChoice()
        }
        binding.cbHp.setOnClickListener {
            handleUserChoice()
        }
        binding.cbDefense.setOnClickListener {
            handleUserChoice()
        }
        binding.btnRandom.setOnClickListener {
            viewModel.getRandomPokemons()
        }
    }

    private fun handleUserChoice() {
        val attack = binding.cbAttack.isChecked
        val defense = binding.cbDefense.isChecked
        val hp = binding.cbHp.isChecked

        if (attack && defense && hp) {
            viewModel.sortPokemonList(UserChoice.ALL)
        } else if (attack && defense) {
            viewModel.sortPokemonList(UserChoice.ATTACK_AND_DEFENSE)
        } else if (attack && hp) {
            viewModel.sortPokemonList(UserChoice.ATTACK_AND_HP)
        } else if (defense && hp) {
            viewModel.sortPokemonList(UserChoice.DEFENSE_AND_HP)
        } else if (attack) {
            viewModel.sortPokemonList(UserChoice.ATTACK)
        } else if (defense) {
            viewModel.sortPokemonList(UserChoice.DEFENSE)
        } else if (hp) {
            viewModel.sortPokemonList(UserChoice.HP)
        }
    }


}