package com.example.studysync.ui

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studysync.databinding.FragmentWikiBinding
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class WikiFragment : Fragment() {

    private var _binding: FragmentWikiBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "WikiFragment"

    private var nextSearchOffset = 0
    private var currentQuery = ""
    private val batchSize = 5
    private var isLoadingMore = false
    private val loadedTitles = mutableSetOf<String>()
    private val items = mutableListOf<WikiArticle>()
    private lateinit var adapter: WikiAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWikiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WikiAdapter(items)
        binding.wikiRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.wikiRecyclerView.adapter = adapter

        binding.searchButton.setOnClickListener {
            val query = binding.searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                startNewSearch(query)  // fetches articles and populates the RecyclerView
            } else {
                Toast.makeText(requireContext(), "Enter a search term.", Toast.LENGTH_SHORT).show()
            }
        }

        setupScrollListener()  // keeps infinite scroll ready, but nothing will load until a search
    }


    private fun startNewSearch(query: String) {
        items.clear()
        loadedTitles.clear()
        adapter.notifyDataSetChanged()
        currentQuery = query
        nextSearchOffset = 0
        fetchNextBatch()
    }

    private fun setupScrollListener() {
        binding.wikiRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMore && lastVisible >= totalItemCount - 2) {
                    fetchNextBatch()
                }
            }
        })
    }

    private fun fetchNextBatch() {
        if (isLoadingMore) return
        isLoadingMore = true

        val encodedQuery = URLEncoder.encode(currentQuery, "UTF-8")
        val searchUrl =
            "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encodedQuery&format=json&sroffset=$nextSearchOffset&srlimit=$batchSize"

        coroutineScope.launch {
            try {
                Log.d(TAG, "Fetching search batch offset=$nextSearchOffset query=$currentQuery")
                val request = Request.Builder().url(searchUrl)
                    .addHeader("User-Agent", "StudySyncApp/1.0")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val searchResults = json.getJSONObject("query").getJSONArray("search")

                if (searchResults.length() == 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No more results.", Toast.LENGTH_SHORT).show()
                    }
                    isLoadingMore = false
                    return@launch
                }

                for (i in 0 until searchResults.length()) {
                    val result = searchResults.getJSONObject(i)
                    val title = result.getString("title")
                    if (!loadedTitles.contains(title)) {
                        loadedTitles.add(title)
                        fetchArticleSummary(title)
                    }
                }

                nextSearchOffset += batchSize

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching search batch.", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoadingMore = false
            }
        }
    }

    private fun fetchArticleSummary(title: String) {
        coroutineScope.launch {
            try {
                val encodedTitle = URLEncoder.encode(title, "UTF-8")
                val url = "https://en.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"
                Log.d(TAG, "Fetching summary for $title: $url")

                val request = Request.Builder().url(url)
                    .addHeader("User-Agent", "StudySyncApp/1.0")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@launch
                val json = JSONObject(body)
                val extract = json.optString("extract", "(No summary available)")

                withContext(Dispatchers.Main) {
                    addArticleToList(title, extract)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addArticleToList(title: String, text: String) {
        items.add(WikiArticle(title, text))
        adapter.notifyItemInserted(items.size - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        coroutineScope.cancel()
    }

    data class WikiArticle(val title: String, val text: String)

    class WikiAdapter(private val items: List<WikiArticle>) :
        RecyclerView.Adapter<WikiAdapter.WikiViewHolder>() {

        class WikiViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout) {
            val titleView = TextView(layout.context)
            val textView = TextView(layout.context)

            init {
                layout.orientation = LinearLayout.VERTICAL
                titleView.textSize = 20f
                titleView.setTypeface(null, Typeface.BOLD)
                titleView.setPadding(0, 16, 0, 8)

                textView.textSize = 16f
                textView.setPadding(0, 0, 0, 16)

                layout.addView(titleView)
                layout.addView(textView)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WikiViewHolder {
            val layout = LinearLayout(parent.context)
            layout.layoutParams =
                RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
            return WikiViewHolder(layout)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: WikiViewHolder, position: Int) {
            val item = items[position]
            holder.titleView.text = item.title
            holder.textView.text = item.text
        }
    }
}
