package com.dranilsaarias.nad


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso


/**
 * A simple [Fragment] subclass.
 */
class ImageFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_image, container, false) as ImageView

        Picasso
                .with(context)
                .load(getString(R.string.dummie_img_url))
                .into(view)

        return view
    }

}// Required empty public constructor