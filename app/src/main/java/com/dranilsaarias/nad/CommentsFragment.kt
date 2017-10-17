package com.dranilsaarias.nad


import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * A simple [Fragment] subclass.
 */
class CommentsFragment : Fragment() {

    private var mContent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mContent = arguments.getString(ARG_CONTENT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_comments, container, false)

        val mail = view.findViewById<TextInputEditText>(R.id.mail)
        mail.setText(mContent)

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
        fun newInstance(content: String): CommentsFragment {
            val fragment = CommentsFragment()
            val args = Bundle()
            args.putString(ARG_CONTENT, content)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
