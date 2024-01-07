package com.hasanbilgin.artbookkotlin.view

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room.databaseBuilder
import com.hasanbilgin.artbookkotlin.R
import com.hasanbilgin.artbookkotlin.adapter.ArtAdapter
import com.hasanbilgin.artbookkotlin.databinding.FragmentListBinding
import com.hasanbilgin.artbookkotlin.model.ArtModel
import com.hasanbilgin.artbookkotlin.roomdb.ArtDao
import com.hasanbilgin.artbookkotlin.roomdb.ArtDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ListFragment : Fragment() {
    private  var _binding: FragmentListBinding?=null
    private val binding get() = _binding!!

    private lateinit var artAdapter: ArtAdapter

    private lateinit var artDatabase : ArtDatabase
    private lateinit var artDao:ArtDao

    private val mDisposable = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //menü için eklendi
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
        artDatabase = databaseBuilder(requireContext(), ArtDatabase::class.java, "Arts").build()
        artDao = artDatabase.artDao()

        getData()

    }

    fun getData() {
        mDisposable.add(
            artDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    private fun handleResponse(artList: List<ArtModel>) {
        binding.listRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        artAdapter = ArtAdapter(artList)
        binding.listRecyclerView.adapter = artAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        val menuInflater = requireActivity().menuInflater
        menuInflater.inflate(R.menu.art_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_art_item) {
            val action = ListFragmentDirections.actionListFragmentToArtFragment("new")
            NavHostFragment.findNavController(this).navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }


}