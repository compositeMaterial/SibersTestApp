package com.killzone.siberstest.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killzone.siberstest.db.Pokemon
import com.killzone.siberstest.network.PokemonList
import com.killzone.siberstest.repositories.MainRepository
import com.killzone.siberstest.repositories.Result
import com.killzone.siberstest.util.Constants.CONTINUOUS_LOAD_STATE
import com.killzone.siberstest.util.Constants.INITIAL_LOAD_STATE
import com.killzone.siberstest.util.SpinnerState
import com.killzone.siberstest.util.UserChoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel @ViewModelInject constructor(
    val repository: MainRepository
) : ViewModel() {

    // This LiveData holds the url to the next page of pokemon list
    private var _pokemonList = MutableLiveData<PokemonList>()
    val pokemonsList: LiveData<PokemonList> = _pokemonList

    // This LiveData holds the list with full info about loaded pokemons
    private var _pokemonInfoList = MutableLiveData<MutableList<Pokemon>>()
    val pokemonInfoList: LiveData<MutableList<Pokemon>> = _pokemonInfoList


    // When it's changed, the UI listens to it and update recycler view list
    private var _isListChanged = MutableLiveData<Boolean>(false)
    val isListChanged: LiveData<Boolean> = _isListChanged

    // UI observes this message, to notify user that loading is failed
    private var _isErrorMessage = MutableLiveData<String?>(null)
    val isErrorMessage: LiveData<String?> = _isErrorMessage

    // Scrolls to the top of recycler view list
    private var _isNeedToScroll = MutableLiveData<Boolean>(false)
    val isNeedToScroll: LiveData<Boolean> = _isNeedToScroll

    // Show load spinner to user, so that he can know that loading occurs
    private var _loadSpinner = MutableLiveData<SpinnerState>(SpinnerState.INVISIBLE)
    val loadSpinner: LiveData<SpinnerState> = _loadSpinner

    // When it's true, UI takes Pokemon object and passes it to another fragment
    private var _navigateToDetailFragment = MutableLiveData<Boolean>()
    val navigateToDetailFragment: LiveData<Boolean> = _navigateToDetailFragment

    // Pokemon object UI passes to DetailFragment
    var pokemonToPass: Pokemon? = null


    // We don't have yet a PokemonList that contains url with next page info.
    // That's why we initially pass INITIAL_LOAD_STATE - we want to initialize
    // PokemonList value to continue loading data in future
    init {
        loadData(INITIAL_LOAD_STATE)
    }



    // Show to user spinner either in the center of the screen, when no data is loaded
    // or in the end of screen, when we loading more
    fun loadData(state: Boolean) {
        when (state) {
            true -> _loadSpinner.value = SpinnerState.INIT_SPINNER
            false -> _loadSpinner.value = SpinnerState.LOAD_SPINNER }

        // Pass to the handle function result of attempt to get PokemonList
        viewModelScope.launch {
            handlePokemonListResponse(state, repository.getPokemonList())
        }
    }


    // If state is INITIAL_LOAD_STATE and Response is successful this function saves
    // data into _pokemonList variable. And then it tries with help of that list upload
    // main list of pokemons with detailed info
    private suspend fun handlePokemonListResponse(state: Boolean, list: Result<PokemonList>) {
        if (state == INITIAL_LOAD_STATE) {
            if (list is Result.Success) {
                _pokemonList.value = list.data
                handlePokemonInfoListResponse(
                    INITIAL_LOAD_STATE,
                    repository.getPokemonInfoList(list.data.convert())
                )
            } else if (list is Result.Error) {
                _isErrorMessage.value = list.exception.toString()
            }
            resetProgressBarValue()

    // When state is different, that means we already have url to next page and we just
    // wanna upload next page and more pokemons

        } else {
            val url = pokemonsList.value?.next

            if (url != null) {
                val nextList = repository.getNextPokemonList(url)
                if (nextList is Result.Success) {
                    _pokemonList.value = nextList.data
                    handlePokemonInfoListResponse(
                        CONTINUOUS_LOAD_STATE,
                        repository.getPokemonInfoList(nextList.data.convert())
                    )
                } else if (nextList is Result.Error) {
                    _isErrorMessage.value = nextList.exception.toString()
                }
            }
            resetProgressBarValue()
        }
    }

    // Second handle-function, that either initialize PokemonInfoList with primary
    // pokemon data or adds to it new data
    private suspend fun handlePokemonInfoListResponse(state: Boolean, list: Result<List<Pokemon>>) {
        if (state == INITIAL_LOAD_STATE) {
            if (list is Result.Success) {
                _pokemonInfoList.value = list.data.toMutableList()
                repository.cachePokemons(list.data)
                _isListChanged.value = true
            } else if (list is Result.Error) {
                _isErrorMessage.value = list.exception.toString()
                _pokemonInfoList.value = repository.getCachedPokemons().toMutableList()
            }
        } else {
            if (list is Result.Success) {
                _pokemonInfoList.value?.addAll(list.data.toMutableList())
                repository.cachePokemons(list.data)
                _isListChanged.value = true
            } else if (list is Result.Error) {
                _isErrorMessage.value = list.exception.toString()
            }
        }
    }

    // If there is no response from network, it tries to load cached data
    fun getCache() {
        viewModelScope.launch {
            _pokemonInfoList.value = repository.getCachedPokemons().toMutableList()
            _isListChanged.value = true
        }
    }

    // Sorts list of pokemons according to user input
    fun sortPokemonList(choice: UserChoice) {
        viewModelScope.launch {
            val list = _pokemonInfoList.value?.toMutableList()

            if (!list.isNullOrEmpty()) {
                withContext(Dispatchers.Default) {
                    if (choice == UserChoice.ATTACK) {
                        list.sortWith(compareBy({ it.attack }))
                    } else if (choice == UserChoice.DEFENSE) {
                        list.sortWith(compareBy({ it.defense }))
                    } else if (choice == UserChoice.HP) {
                        list.sortWith(compareBy({ it.hp }))
                    } else if (choice == UserChoice.ATTACK_AND_DEFENSE) {
                        list.sortWith(compareBy({ it.attack }, { it.defense }))
                    } else if (choice == UserChoice.ATTACK_AND_HP) {
                        list.sortWith(compareBy({ it.attack }, { it.hp }))
                    } else if (choice == UserChoice.DEFENSE_AND_HP) {
                        list.sortWith(compareBy({ it.defense }, { it.hp }))
                    } else if (choice == UserChoice.ALL) {
                        list.sortWith(compareBy({ it.attack }, { it.defense }, { it.hp }))
                    }
                }
                _pokemonInfoList.value = list.reversed().toMutableList()
                _isListChanged.value = true
                delay(500)
                _isNeedToScroll.value = true
            }
        }
    }

    // Function that gets random list of pokemons. Handles result with help
    // already familiar handle-functions
    fun getRandomPokemons() {
        viewModelScope.launch {
            _loadSpinner.value = SpinnerState.INIT_SPINNER
            handlePokemonListResponse(INITIAL_LOAD_STATE, repository.getRandomPokemonList())
            resetProgressBarValue()
        }
    }

    // Functions that is passed to RecyclerView Adapter. Inside, it gets proper
    // Pokemon item and switches navigation value, that is being listened by UI
    fun onPokemonClicked(pokemon: Pokemon) {
        pokemonToPass = pokemon
        _navigateToDetailFragment.value = true
    }

    // Reset functions helps UI to interact with ViewModel without unwanted
    // direct access to its data
    fun resetProgressBarValue() { _loadSpinner.value = SpinnerState.INVISIBLE }
    fun resetNavigateValue() { _navigateToDetailFragment.value = false }
    fun resetListChangedValue() { _isListChanged.value = false }
    fun resetScrollValue() { _isNeedToScroll.value = false }
}
