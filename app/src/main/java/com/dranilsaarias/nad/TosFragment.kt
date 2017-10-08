package com.dranilsaarias.nad


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluejamesbond.text.DocumentView


/**
 * A simple [Fragment] subclass.
 */
class TosFragment : Fragment() {

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
        val view = inflater!!.inflate(R.layout.fragment_tos, container, false)

        val tosText = view.findViewById<DocumentView>(R.id.tos_text)
        tosText.text = mContent

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
        fun newInstance(content: String): TosFragment {
            val fragment = TosFragment()
            val args = Bundle()
            args.putString(ARG_CONTENT, content)
            fragment.arguments = args
            return fragment
        }
    }

}
