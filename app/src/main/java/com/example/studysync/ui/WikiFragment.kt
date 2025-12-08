package com.example.studysync.ui

import android.graphics.Typeface
import android.util.Log
import android.os.Bundle
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
import org.jsoup.Jsoup
import org.json.JSONObject
import java.net.URLEncoder

class WikiFragment : Fragment() {

    private var _binding: FragmentWikiBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val loadedTitles = mutableSetOf<String>()
    private lateinit var adapter: WikiAdapter
    private val items = mutableListOf<WikiArticle>()
    private var isLoadingMore = false

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
        fetchArticle(query)
    }

    private fun parseHtmlToSections(html: String): List<WikiItem> {
        val doc = Jsoup.parse(html)
        val items = mutableListOf<WikiItem>()

        // Select headings and paragraphs in order
        doc.body().children().forEach { element ->
            when {
                element.tagName().matches(Regex("h[1-6]")) -> {
                    val text = element.text()
                    if (text.isNotBlank()) items.add(WikiItem.Section(text))
                }
                element.tagName() == "p" -> {
                    val text = element.text()
                    if (text.isNotBlank()) items.add(WikiItem.Paragraph(text))
                }
            }
        }
        return items
    }

    private fun setupScrollListener() {
        binding.wikiRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMore && lastVisible >= totalItemCount - 2) {
                    // Load next batch of related articles
                    val nextTitle = loadedTitles.lastOrNull() ?: return
                    fetchRelatedArticles(nextTitle)
                }
            }
        })
    }
    private val TAG = "WikiFragment"
    private fun fetchArticle(title: String) {
        isLoadingMore = true
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        val url =
            "https://en.wikipedia.org/w/api.php?action=parse&page=$encodedTitle&format=json&prop=text&formatversion=2"
        Log.d(TAG, "Fetching article: $title at URL: $url")
        coroutineScope.launch {
            try {
                val request = Request.Builder().url(url)
                    .addHeader("User-Agent", "StudySyncApp/1.0")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                val json = JSONObject(body)
                val htmlContent = json.getJSONObject("parse").getString("text")
                val plainText = Jsoup.parse(htmlContent).text()

                loadedTitles.add(title)
                withContext(Dispatchers.Main) {
                    addArticleToList(title, plainText)
                }

                fetchRelatedArticles(title)

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching article: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoadingMore = false
            }
        }
    }

    private fun fetchRelatedArticles(title: String) {
        isLoadingMore = true
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        val url = "https://en.wikipedia.org/api/rest_v1/page/related/$encodedTitle"

        coroutineScope.launch {
            try {
                val request = Request.Builder().url(url)
                    .addHeader("User-Agent", "StudySyncApp/1.0")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@launch

                val relatedArray = JSONObject(body).optJSONArray("pages") ?: return@launch
                for (i in 0 until relatedArray.length()) {
                    val obj = relatedArray.getJSONObject(i)
                    val relatedTitle = obj.getString("title")
                    if (!loadedTitles.contains(relatedTitle)) {
                        fetchArticle(relatedTitle)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingMore = false
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
