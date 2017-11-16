package com.dranilsaarias.nad


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class ImageFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_image, container, false) as ImageView

        val ad = JSONObject(arguments.getString(ARG_CONTENT))
        val url = getString(R.string.host, ad.getString("img"))

        view.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.getString("url")))
            startActivity(browserIntent)
        }

        Picasso
                .with(context)
                .load(url)
                .placeholder(R.drawable.login_logo)
                .noFade()
                .into(view)

        return view
    }

    companion object {
        private val ARG_CONTENT = "content"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param content Parameter 1.
         * *
         * *
         * @return A new instance of fragment CommentsFragment.
         */
        fun newInstance(content: String): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putString(ARG_CONTENT, content)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor