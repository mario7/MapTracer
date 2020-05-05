package com.futurelinegen.maptracer.relam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.futurelinegen.maptracer.R
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter
import android.text.format.DateFormat
import android.util.Log

class ViewHolder(view: View) {
    val textMapId: TextView = view.findViewById(R.id.textMapId)
    val textTimeStamp: TextView = view.findViewById(R.id.textTimeStamp)
    val titleLatitude: TextView = view.findViewById(R.id.titleLatitude)
    val textLatitude: TextView = view.findViewById(R.id.textLatitude)
    val titleLongatitude: TextView = view.findViewById(R.id.titleLongatitude)
    val textLongitude: TextView = view.findViewById(R.id.textLongitude)
}

class MapListAdapter(realmResult: OrderedRealmCollection<LocationModel>): RealmBaseAdapter<LocationModel>(realmResult){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //To change body of created functions use File | Settings | File Templates.
        val vh: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.item_location, parent, false)

            vh = ViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        adapterData?.get(position)?.let {
            vh.textLatitude.text = it.latitude.toString()
            vh.textLongitude.text = it.longtitude.toString()
            //vh.textTimeStamp.text = it.date
            vh.textTimeStamp.text = DateFormat.format("yyyy/MM/dd", it.date)
            vh.textMapId.text = it.mapId.toString()

            Log.d("mapLocation", "MapListAdapter mId=${it.mapId}, date=${it.date}" )
        }

        return view
    }

    override fun getItemId(position: Int): Long {
        return  adapterData?.get(position)?.mapId ?: super.getItemId(position)
    }

    fun setResults(realmResult: OrderedRealmCollection<LocationModel>) {
        adapterData = realmResult
        notifyDataSetChanged()
    }

}