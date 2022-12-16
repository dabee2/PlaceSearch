package com.dabee.tpquickplacebykakaosearchapi.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabee.tpquickplacebykakaosearchapi.R
import com.dabee.tpquickplacebykakaosearchapi.activities.PlaceUrlActivity
import com.dabee.tpquickplacebykakaosearchapi.databinding.RecyclerItemListFragmentBinding
import com.dabee.tpquickplacebykakaosearchapi.model.Place

class PlaceListRecyclerAdapter (val context:Context, val documents:MutableList<Place>) : RecyclerView.Adapter<PlaceListRecyclerAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding by lazy { RecyclerItemListFragmentBinding.bind(itemView) }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView:View = LayoutInflater.from(context).inflate(R.layout.recycler_item_list_fragment,parent,false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        var place = documents[position]

        holder.binding.tvPlaceName.text = place.place_name
        holder.binding.tvAddress.text = if(place.road_address_name=="") place.address_name else place.road_address_name
        holder.binding.tvPlaceDistance.text = "${place.distance}m"
        holder.binding.tvPhone.text = place.phone

        // 아이템뷰를 클릭했을때 장소에 대한
        holder.itemView.setOnClickListener{
            val intent:Intent = Intent(context,PlaceUrlActivity::class.java)
            intent.putExtra("place_url",place.place_url)
            context.startActivity(intent)
        }



    }

    override fun getItemCount(): Int = documents.size


}