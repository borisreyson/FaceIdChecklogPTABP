package com.misit.abpenergy.IntroFragmentimport android.view.LayoutInflaterimport android.view.Viewimport android.view.ViewGroupimport android.widget.TextViewimport androidx.recyclerview.widget.RecyclerViewimport com.misit.abpenergy.Rclass IntroSliderAdapter(private val introSlide: List<IntroSlide>)    : RecyclerView.Adapter<IntroSliderAdapter.introSliderViewHolder>() {    inner class introSliderViewHolder(view:View):RecyclerView.ViewHolder(view){        private val textTitle = view.findViewById<TextView>(R.id.fragment_title)        private val textContent = view.findViewById<TextView>(R.id.fragment_content)        private val textAsk = view.findViewById<TextView>(R.id.fragment_ask)        fun bind(introSlide: IntroSlide){            textTitle.text = introSlide.textTitle            textContent.text = introSlide.textContent            textAsk.text = introSlide.textAsk        }    }    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): introSliderViewHolder {        return introSliderViewHolder(            LayoutInflater.from(parent.context).inflate(R.layout.fragment_slider,parent,false)        )    }    override fun onBindViewHolder(holder: introSliderViewHolder, position: Int) {        holder.bind(introSlide[position])    }    override fun getItemCount(): Int {        return introSlide.size    }}