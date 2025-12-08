package com.example.studysync.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.studysync.databinding.FragmentWikiBinding
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLEncoder

class WikiFragment : Fragment() {

    private var _binding: FragmentWikiBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "WikiFragment"

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

        binding.searchButton.setOnClickListener {
            val query = binding.searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                fetchFullText(query)
            } else {
                binding.wikiText.text = "Please enter a search term."
            }
        }
    }

    private fun fetchFullText(query: String) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url =
            "https://en.wikipedia.org/w/api.php?action=parse&page=$encodedQuery&format=json&prop=text&formatversion=2"
        Log.d(TAG, "Fetching Wikipedia full text at URL: $url")

        coroutineScope.launch {
            try {
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "StudySyncApp/1.0 (contact: your_email@example.com)")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (body.isNullOrEmpty()) {
                    showError("No response from Wikipedia.")
                    return@launch
                }

                val json = JSONObject(body)
                val htmlContent = json.getJSONObject("parse").getString("text")

                // Use Jsoup to parse HTML and extract plain text
                val plainText = Jsoup.parse(htmlContent).text()

                withContext(Dispatchers.Main) {
                    binding.wikiText.text = plainText
                    binding.scrollView.scrollTo(0, 0)
                }

            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "DNS/network failure", e)
                showError("Network error: unable to reach Wikipedia. Check your connection.")
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching article", e)
                showError("Error loading article: ${e.localizedMessage}")
            }
        }
    }

    private fun showError(message: String) {
        activity?.runOnUiThread {
            binding.wikiText.text = message
            binding.scrollView.scrollTo(0, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        coroutineScope.cancel()
    }
}
