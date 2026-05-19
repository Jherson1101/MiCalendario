package com.example.micalendario.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.micalendario.R;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Task;

import android.content.Intent;
import com.example.micalendario.activities.EditTaskActivity;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    Context context;
    List<Task> listaTasks;
    SQLiteHelper sqLiteHelper;

    public TaskAdapter(Context context, List<Task> listaTasks) {
        this.context = context;
        this.listaTasks = listaTasks;
        sqLiteHelper = new SQLiteHelper(context);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task,
                        parent,
                        false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        Task task = listaTasks.get(position);
        holder.tvTitulo.setText(task.getTitulo());
        holder.tvDescripcion.setText(task.getDescripcion());
        holder.tvHora.setText(task.getHora());
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, EditTaskActivity.class);
            intent.putExtra("id", task.getId());
            intent.putExtra("titulo", task.getTitulo());
            intent.putExtra("descripcion", task.getDescripcion());
            intent.putExtra("hora", task.getHora());
            intent.putExtra("periodo", task.getPeriodo());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            mostrarDialogoEliminar(task, position);
            return true;
        });
    }

    private void mostrarDialogoEliminar(Task task, int position){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar");
        builder.setMessage(
                "¿Desea eliminar esta actividad?"
        );
        builder.setPositiveButton("Sí", (dialog, which) -> {

            sqLiteHelper.eliminarTask(task.getId());
            listaTasks.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,
                    listaTasks.size());

        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        return listaTasks.size();
    }
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        TextView tvDescripcion;
        TextView tvHora;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloTask);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTask);
            tvHora = itemView.findViewById(R.id.tvHoraTask);
        }
    }
}
