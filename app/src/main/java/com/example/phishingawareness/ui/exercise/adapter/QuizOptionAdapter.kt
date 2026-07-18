package com.example.phishingawareness.ui.exercise.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.phishingawareness.databinding.ItemQuizOptionBinding
import com.example.phishingawareness.domain.model.QuizOption

class QuizOptionAdapter(
    private val onOptionCheckedChange: (
        optionId: String,
        isChecked: Boolean
    ) -> Unit
) : RecyclerView.Adapter<QuizOptionAdapter.QuizOptionViewHolder>() {

    private var options: List<QuizOption> = emptyList()

    private var selectedOptionIds: Set<String> = emptySet()

    fun submitData(
        newOptions: List<QuizOption>,
        newSelectedOptionIds: Set<String>
    ) {
        options = newOptions
        selectedOptionIds = newSelectedOptionIds

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): QuizOptionViewHolder {
        val binding = ItemQuizOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return QuizOptionViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: QuizOptionViewHolder,
        position: Int
    ) {
        val option = options[position]

        holder.bind(
            option = option,
            isSelected = option.id in selectedOptionIds
        )
    }

    override fun getItemCount(): Int {
        return options.size
    }

    inner class QuizOptionViewHolder(
        private val binding: ItemQuizOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            option: QuizOption,
            isSelected: Boolean
        ) {
            /*
             * Il listener viene rimosso prima di modificare isChecked.
             * In questo modo il riciclo della View non produce eventi
             * interpretati erroneamente come azioni dell'utente.
             */
            binding.optionCheckBox.setOnCheckedChangeListener(null)

            binding.optionCheckBox.text = option.text
            binding.optionCheckBox.isChecked = isSelected

            binding.optionCheckBox.setOnCheckedChangeListener {
                    _,
                    isChecked ->

                if (isChecked != isSelected) {
                    onOptionCheckedChange(
                        option.id,
                        isChecked
                    )
                }
            }
        }
    }
}