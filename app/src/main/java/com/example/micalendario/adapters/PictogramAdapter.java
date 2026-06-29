package com.example.micalendario.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.micalendario.R;
import com.example.micalendario.models.Pictogram;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class PictogramAdapter extends RecyclerView.Adapter<PictogramAdapter.ViewHolder> {

    private final List<Pictogram> pictograms;
    private String selectedPictogram;
    private final OnPictogramClickListener listener;

    public interface OnPictogramClickListener {
        void onPictogramClick(Pictogram pictogram);
    }

    public PictogramAdapter(List<Pictogram> pictograms, String selectedPictogram, OnPictogramClickListener listener) {
        this.pictograms = pictograms;
        this.selectedPictogram = selectedPictogram;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pictogram_selector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pictogram pictogram = pictograms.get(position);
        
        if (pictogram.isCustom()) {
            // Cargar imagen desde archivo para pictogramas personalizados
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(pictogram.getImagePath());
            holder.imageView.setImageBitmap(bitmap);
            holder.textView.setText(pictogram.getName());
        } else {
            // Cargar desde recursos para pictogramas predeterminados
            holder.imageView.setImageResource(pictogram.getImageResId());
            holder.textView.setText(pictogram.getLabelResId());
        }

        boolean isSelected = pictogram.getName().equals(selectedPictogram);
        if (isSelected) {
            holder.cardView.setStrokeColor(holder.itemView.getContext().getColor(R.color.blue_primary));
            holder.cardView.setStrokeWidth(4);
            holder.cardView.setCardElevation(8f);
        } else {
            holder.cardView.setStrokeWidth(0);
            holder.cardView.setCardElevation(2f);
        }

        holder.cardView.setOnClickListener(v -> {
            selectedPictogram = pictogram.getName();
            notifyDataSetChanged();
            listener.onPictogramClick(pictogram);
        });
    }

    @Override
    public int getItemCount() {
        return pictograms.size();
    }

    public void setSelectedPictogram(String selectedPictogram) {
        this.selectedPictogram = selectedPictogram;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardPicto);
            imageView = itemView.findViewById(R.id.imgPicto);
            textView = itemView.findViewById(R.id.tvPictoLabel);
        }
    }
}
