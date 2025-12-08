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
import com.example.studysync.databinding.FragmentResourcesBinding
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class ResourcesFragment : Fragment() {

    private var _binding: FragmentResourcesBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val batchSize = 5
    private var nextSearchOffset = 0
    private var currentQuery = ""
    private var isLoadingMore = false
    private val loadedTitles = mutableSetOf<String>()
    private val items = mutableListOf<WikiItem>()
    private lateinit var adapter: WikiAdapter
    private val TAG = "ResourcesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResourcesBinding.inflate(inflater, container, false)
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
                Log.d(TAG, "Starting new search for query: $query")
                startNewSearch(query)
            } else {
                Toast.makeText(requireContext(), "Enter a search term.", Toast.LENGTH_SHORT).show()
            }
        }

        setupScrollListener()
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
                    Log.d(TAG, "Scrolled near bottom. Fetching next batch.")
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

        Log.d(TAG, "Fetching batch: offset=$nextSearchOffset, query=$currentQuery")

        coroutineScope.launch {
            try {
                val request = Request.Builder().url(searchUrl)
                    .addHeader("User-Agent", "StudySyncApp/1.0").build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                val searchResults = JSONObject(body).getJSONObject("query").getJSONArray("search")

                if (searchResults.length() == 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No more results.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "No more results returned from Wikipedia API.")
                    }
                    isLoadingMore = false
                    return@launch
                }

                for (i in 0 until searchResults.length()) {
                    val result = searchResults.getJSONObject(i)
                    val title = result.getString("title")
                    if (!loadedTitles.contains(title)) {
                        loadedTitles.add(title)
                        Log.d(TAG, "Fetching summary for article: $title")
                        fetchArticleSummary(title)
                    }
                }

                nextSearchOffset += batchSize
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching search batch.", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error fetching batch: ${e.message}")
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
                val request = Request.Builder().url(url)
                    .addHeader("User-Agent", "StudySyncApp/1.0").build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@launch
                val json = JSONObject(body)
                val extract = json.optString("extract", "(No summary available)")

                withContext(Dispatchers.Main) {
                    items.add(WikiItem.Section(title))
                    items.add(WikiItem.Paragraph(extract))
                    adapter.notifyItemRangeInserted(items.size - 2, 2)
                    Log.d(TAG, "Added article to list: $title")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Error fetching summary for $title: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        coroutineScope.cancel()
    }

    // Adapter for multiple view types
    class WikiAdapter(private val items: List<WikiItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_SECTION = 0
            private const val TYPE_PARAGRAPH = 1
        }

        inner class SectionViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout) {
            val titleView = TextView(layout.context)
            init {
                layout.orientation = LinearLayout.VERTICAL
                titleView.textSize = 20f
                titleView.setTypeface(null, Typeface.BOLD)
                titleView.setPadding(0, 16, 0, 8)
                layout.addView(titleView)
            }
        }

        inner class ParagraphViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout) {
            val textView = TextView(layout.context)
            init {
                layout.orientation = LinearLayout.VERTICAL
                textView.textSize = 16f
                textView.setPadding(0, 0, 0, 16)
                layout.addView(textView)
            }
        }

        override fun getItemViewType(position: Int): Int =
            when (items[position]) {
                is WikiItem.Section -> TYPE_SECTION
                is WikiItem.Paragraph -> TYPE_PARAGRAPH
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layout = LinearLayout(parent.context)
            layout.layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            return when (viewType) {
                TYPE_SECTION -> SectionViewHolder(layout)
                TYPE_PARAGRAPH -> ParagraphViewHolder(layout)
                else -> throw IllegalArgumentException("Unknown view type")
            }
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is WikiItem.Section -> (holder as SectionViewHolder).titleView.text = item.title
                is WikiItem.Paragraph -> (holder as ParagraphViewHolder).textView.text = item.text
            }
        }
    }
}
