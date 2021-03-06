package com.henrique.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "CrimeListFragment"

private const val VIEW_TYPE_CRIME = 0
private const val VIEW_TYPE_POLICE_CRIME = 1

class CrimeListFragment : Fragment() {

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter = CrimeAdapter()

    private val crimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()

        callbacks = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)

        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            { crimes ->
                crimes.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    adapter.submitList(crimes)
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                onClickNewCrime()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onClickNewCrime() {
        val crime = Crime()

        crimeListViewModel.addCrime(crime)
        callbacks?.onCrimeSelected(crime.id)
    }

    private inner class CrimeHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        private lateinit var crime: Crime

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime

            titleTextView.text = crime.title
            dateTextView.text = crime.formattedDate()

            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeAdapter() :
        ListAdapter<Crime, CrimeHolder>(CrimeDiffCallback()) {

        override fun getItemViewType(position: Int): Int {
            val crime = getItem(position)

            return VIEW_TYPE_CRIME
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val crimeLayout = when (viewType) {
                VIEW_TYPE_CRIME -> R.layout.list_item_crime
                VIEW_TYPE_POLICE_CRIME -> R.layout.list_item_crime
                else -> throw IllegalStateException("Unknown viewType $viewType")
            }
            val view = layoutInflater.inflate(crimeLayout, parent, false)

            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = getItem(position)

            holder.bind(crime)
        }
    }

    companion object {
        fun newInstance() = CrimeListFragment()
    }
}

class CrimeDiffCallback : DiffUtil.ItemCallback<Crime>() {
    override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return oldItem == newItem
    }
}
