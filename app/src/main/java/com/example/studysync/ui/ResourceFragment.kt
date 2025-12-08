package com.example.studysync.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class ResourceFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val items = mutableListOf<WikiPage>()
    private val TAG = "ResourceFragment"

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): LinearLayout {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        // Input field
        val editText = EditText(requireContext())
        editText.hint = "Enter subject"
        layout.addView(editText)

        val searchButton = Button(requireContext())
        searchButton.text = "Search"
        layout.addView(searchButton)

        recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = WikiAdapter(items)
        layout.addView(recyclerView)

        searchButton.setOnClickListener {
            val topic = editText.text.toString().trim()
            if (topic.isNotEmpty()) {
                fetchPages(topic)
            } else {
                Toast.makeText(requireContext(), "Enter a topic", Toast.LENGTH_SHORT).show()
            }
        }

        return layout
    }

    private fun fetchPages(topic: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                items.clear()

                // Fetch main page summary
                val mainUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/${topic.replace(" ", "_")}"
                val mainResponse = URL(mainUrl).readText()
                val mainJson = JSONObject(mainResponse)
                val mainTitle = mainJson.getString("title")
                val mainExtract = mainJson.optString("extract", "(No summary)").split(" ").take(500).joinToString(" ")
                val mainThumbnail = mainJson.optJSONObject("thumbnail")?.optString("source")
                val mainUrlLink = mainJson.getJSONObject("content_urls").getJSONObject("desktop").getString("page")
                items.add(WikiPage(mainTitle, mainExtract, mainThumbnail, mainUrlLink))

                // Fetch 2 related pages
                val relatedUrl = "https://en.wikipedia.org/api/rest_v1/page/related/${topic.replace(" ", "_")}"
                val relatedResponse = URL(relatedUrl).readText()
                val relatedArray = JSONObject(relatedResponse).optJSONArray("pages")

                if (relatedArray != null) {
                    for (i in 0 until minOf(2, relatedArray.length())) {
                        val rel = relatedArray.getJSONObject(i)
                        val relTitle = rel.getString("title")
                        val relExtract = rel.optString("extract", "(No summary)").split(" ").take(500).joinToString(" ")
                        val relThumbnail = rel.optJSONObject("thumbnail")?.optString("source")
                        val relUrlLink = rel.getJSONObject("content_urls").getJSONObject("desktop").getString("page")
                        items.add(WikiPage(relTitle, relExtract, relThumbnail, relUrlLink))
                    }
                }

                withContext(Dispatchers.Main) {
                    recyclerView.adapter?.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching pages", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching pages: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    data class WikiPage(
        val title: String,
        val extract: String,
        val thumbnail: String?,
        val url: String
    )

    class WikiAdapter(private val items: List<WikiPage>) :
        RecyclerView.Adapter<WikiAdapter.ViewHolder>() {

        class ViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout) {
            val titleView = TextView(layout.context)
            val extractView = TextView(layout.context)
            val imageView = ImageView(layout.context)

            init {
                layout.orientation = LinearLayout.VERTICAL
                titleView.textSize = 20f
                titleView.setPadding(0, 0, 0, 10)
                titleView.setTypeface(null, android.graphics.Typeface.BOLD)

                extractView.textSize = 16f
                layout.addView(titleView)
                layout.addView(imageView)
                layout.addView(extractView)
                layout.setPadding(20, 20, 20, 20)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layout = LinearLayout(parent.context)
            layout.layoutParams =
                RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
            return ViewHolder(layout)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.titleView.text = item.title
            holder.extractView.text = item.extract

            if (!item.thumbnail.isNullOrEmpty()) {
                Glide.with(holder.imageView.context)
                    .load(item.thumbnail)
                    .centerCrop()
                    .override(500, 250)
                    .into(holder.imageView)
            } else {
                holder.imageView.setImageDrawable(null)
            }

            holder.layout.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                it.context.startActivity(intent)
            }
        }
    }
}
