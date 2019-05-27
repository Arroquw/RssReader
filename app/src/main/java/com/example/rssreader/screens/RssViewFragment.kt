package com.example.rssreader.screens

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.example.rssreader.MainActivity
import com.example.rssreader.R
import kotlinx.android.synthetic.main.fragment_rss_view.*


class RssViewFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val webView = fragmentWebView
        assert(arguments != null)
        val args = RssViewFragmentArgs.fromBundle(arguments!!)

        val postContent = args.rssLink
        val postTitle = args.rssTitle

        (activity as MainActivity?)!!.updateTitle(postTitle);

        if (Patterns.WEB_URL.matcher(postContent.toLowerCase()).matches()) {
            webView.loadUrl(postContent)
        } else {
            webView.loadData(postContent, "text/html; charset=utf-8", "utf-8")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rss_view, container, false)
    }
}// Required empty public constructor