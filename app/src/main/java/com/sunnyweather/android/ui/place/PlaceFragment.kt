package com.sunnyweather.android.ui.place

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R

class PlaceFragment: Fragment(R.layout.fragment_place) {

//    val viewModel: PlaceViewModel by viewModels()
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    private lateinit var adapter: PlaceAdapter

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_place,container,false)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager= LinearLayoutManager(requireContext())
        val recyclerView: RecyclerView=view.findViewById<RecyclerView>(R.id.recycleView)
        recyclerView.layoutManager=layoutManager
        adapter= PlaceAdapter(this,viewModel.placeList)
        recyclerView.adapter=adapter
        val bgImageView: ImageView=view.findViewById<ImageView>(R.id.bgImageView)

        val searchPlaceEdit: EditText=view.findViewById<EditText>(R.id.searchPlaceEdit)
        searchPlaceEdit.addTextChangedListener{editable ->
            val content=editable.toString()
            if (content.isNotEmpty()){
                viewModel.searchPlaces(content)
            }else{
                recyclerView.visibility=View.GONE
                bgImageView.visibility=View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.placeLiveData.observe(viewLifecycleOwner){result ->
            val places=result.getOrNull()
            if(places!=null){
                recyclerView.visibility= View.VISIBLE
                bgImageView.visibility= View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            }else{
                Toast.makeText(requireContext(),"未查询到地点", Toast.LENGTH_SHORT).show()

                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

}