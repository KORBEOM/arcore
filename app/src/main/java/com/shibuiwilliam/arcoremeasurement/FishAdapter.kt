package com.shibuiwilliam.arcoremeasurement

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FishAdapter(private val fishList: List<Bottomfrgment_one.FishData>) : RecyclerView.Adapter<FishAdapter.FishViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fish_info_item, parent, false)
        return FishViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: FishViewHolder, position: Int) {
        val fish = fishList[position]
        holder.fishNameTextView.text = fish.name
        holder.fishHabitatTextView.text = fish.habitat
        holder.fishSizeTextView.text = fish.size
        holder.fishImageView.setImageResource(fish.imageResId)

        // Variable to track whether the image is enlarged
        var isEnlarged = false

        // Add touch listener to enlarge and shrink the image on touch
        holder.fishImageView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val scaleAnimation: ScaleAnimation = if (isEnlarged) {
                    ScaleAnimation(
                        1.5f, 1f, // Start and end values for the X axis scaling
                        1.5f, 1f, // Start and end values for the Y axis scaling
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f // Pivot point of Y scaling
                    )
                } else {
                    ScaleAnimation(
                        1f, 1.5f, // Start and end values for the X axis scaling
                        1f, 1.5f, // Start and end values for the Y axis scaling
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f // Pivot point of Y scaling
                    )
                }
                scaleAnimation.fillAfter = true // Needed to keep the result of the animation
                scaleAnimation.duration = 300
                v.startAnimation(scaleAnimation)

                // Toggle the state
                isEnlarged = !isEnlarged
            }
            false
        }
    }

    override fun getItemCount(): Int {
        return fishList.size
    }

    class FishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fishNameTextView: TextView = itemView.findViewById(R.id.fishNameTextView)
        val fishHabitatTextView: TextView = itemView.findViewById(R.id.fishHabitatTextView)
        val fishSizeTextView: TextView = itemView.findViewById(R.id.fishSizeTextView)
        val fishImageView: ImageView = itemView.findViewById(R.id.fishImageView)
    }
}