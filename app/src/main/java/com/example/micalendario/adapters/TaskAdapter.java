package com.example.micalendario.adapters;

import com.google.android.material.card.MaterialCardView;

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

import android.widget.ImageView;
import android.widget.CheckBox;

import com.example.micalendario.utils.NotificationHelper;
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    Context context;
    List<Task> listaTasks;
    SQLiteHelper sqLiteHelper;
    String rol;
    boolean modoOscuro;

    // constructor del adaptador
    public TaskAdapter(Context context, List<Task> listaTasks, String rol, boolean modoOscuro) {
        this.context = context;
        this.listaTasks = listaTasks;
        this.rol = rol;
        this.modoOscuro = modoOscuro;
        sqLiteHelper = new SQLiteHelper(context);
    }
    // creamos cada item del recyclerview
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task,
                        parent,
                        false);
        return new TaskViewHolder(view);
    }
    // asigna datos a cada item
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        Task task = listaTasks.get(position);

        holder.tvTitulo.setText(task.getTitulo());
        holder.tvDescripcion.setText(task.getDescripcion());
        holder.tvHora.setText(task.getHora());

        // SELECCIÓN DE PICTOGRAMA (HU 8)
        String picto = task.getPictograma();
        if (picto != null && !picto.isEmpty()) {
            // Si hay un pictograma guardado, lo buscamos en los recursos
            int resId = context.getResources().getIdentifier(
                    picto,
                    "drawable",
                    context.getPackageName()
            );
            if (resId != 0) {
                holder.imgTask.setImageResource(resId);
            } else {
                asignarPictogramaAutomatico(holder, task.getTitulo().toLowerCase());
            }
        } else {
            // Si no hay pictograma guardado, usamos la lógica automática
            asignarPictogramaAutomatico(holder, task.getTitulo().toLowerCase());
        }

        // checkbox de completado
        holder.checkCompletada.setChecked(task.getCompletada() == 1);
        if(task.getCompletada() == 1){
            holder.cardTask.setAlpha(0.5f);
        } else {
            holder.cardTask.setAlpha(1f);
        }

        holder.checkCompletada.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    int estado = isChecked ? 1 : 0;
                    sqLiteHelper.actualizarEstadoTask(task.getId(), estado);
                    task.setCompletada(estado);
                    if(isChecked){
                        holder.cardTask.setAlpha(0.5f);
                    } else {
                        holder.cardTask.setAlpha(1f);
                    }
                });

        // condicional para que funcione el modo oscuro
        if(modoOscuro){
            holder.cardTask.setCardBackgroundColor(
                    context.getColor(R.color.dark_card)
            );
            holder.tvTitulo.setTextColor(
                    context.getColor(R.color.dark_text)
            );
            holder.tvDescripcion.setTextColor(
                    context.getColor(R.color.dark_text)
            );
            holder.tvHora.setTextColor(
                    context.getColor(R.color.white)
            );
        } else {
            // COLORES POR PERIODO
            String periodo = task.getPeriodo().toLowerCase();
            if(periodo.contains("mañana")){
                holder.cardTask.setCardBackgroundColor(
                        context.getColor(R.color.morning_color)
                );
            } else if(periodo.contains("tarde")){
                holder.cardTask.setCardBackgroundColor(
                        context.getColor(R.color.afternoon_color)
                );
            } else if(periodo.contains("noche")){
                holder.cardTask.setCardBackgroundColor(
                        context.getColor(R.color.night_color)
                );
            }
        }


        // al momento de presionar las actividades, debe salir aqui
        holder.cardTask.setOnClickListener(v -> {
            if(!rol.equals("cuidador")){
                return;
            }
            Intent intent = new Intent(context, EditTaskActivity.class);
            intent.putExtra("id", task.getId());
            intent.putExtra("titulo", task.getTitulo());
            intent.putExtra("descripcion", task.getDescripcion());
            intent.putExtra("hora", task.getHora());
            intent.putExtra("periodo", task.getPeriodo());
            intent.putExtra("fecha", task.getFecha());
            intent.putExtra("modoOscuro", modoOscuro);
            context.startActivity(intent);
        });
        holder.cardTask.setOnLongClickListener(v -> {
            if(!rol.equals("cuidador")){
                return true;
            }
            mostrarDialogoEliminar(task, position);
            return true;
        });
    }
    // mensaje para eliminar tarea
    private void mostrarDialogoEliminar(Task task, int position){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar");
        builder.setMessage(
                "¿Desea eliminar esta actividad?"
        );
        builder.setPositiveButton("Sí", (dialog, which) -> {

            sqLiteHelper.eliminarTask(task.getId());

            // alerta por los cambios
            NotificationHelper.mostrarNotificacion(
                    context,
                    "Actividad eliminada",
                    task.getTitulo()
            );

            listaTasks.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,
                    listaTasks.size());

        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
    // cantidad de elementos y retorna la cantidad de tareas
    @Override
    public int getItemCount() {
        return listaTasks.size();
    }

    private void asignarPictogramaAutomatico(TaskViewHolder holder, String titulo) {
        if(titulo.contains("desayuno") || titulo.contains("comer") || titulo.contains("almuerzo") || titulo.contains("cena") || titulo.contains("alimento")){
            holder.imgTask.setImageResource(R.drawable.desayuno);
        } else if(titulo.contains("dientes") || titulo.contains("cepillarse")){
            holder.imgTask.setImageResource(R.drawable.dientes);
        } else if(titulo.contains("tarea") || titulo.contains("estudiar") || titulo.contains("leer") || titulo.contains("colegio") || titulo.contains("escuela")){
            holder.imgTask.setImageResource(R.drawable.estudiar);
        } else if(titulo.contains("jugar") || titulo.contains("pelota") || titulo.contains("juego")){
            holder.imgTask.setImageResource(R.drawable.jugar);
        } else if(titulo.contains("bano") || titulo.contains("ducha")){
            holder.imgTask.setImageResource(R.drawable.ducha);
        } else if(titulo.contains("dormir") || titulo.contains("descansar") || titulo.contains("cama")){
            holder.imgTask.setImageResource(R.drawable.dormir);
        } else if(titulo.contains("medicina") || titulo.contains("pastilla")){
            holder.imgTask.setImageResource(R.drawable.medicina);
        } else if (titulo.contains("ejercicio") || titulo.contains("deporte")){
            holder.imgTask.setImageResource(R.drawable.ejercicio);
        } else if(titulo.contains("terapia") || titulo.contains("psicologo")){
            holder.imgTask.setImageResource(R.drawable.terapia);
        } else {
            holder.imgTask.setImageResource(R.drawable.desayuno);
        }
    }

    // clase que almacena referencias visuales
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        TextView tvDescripcion;
        TextView tvHora;
        ImageView imgTask;
        MaterialCardView cardTask;
        CheckBox checkCompletada;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloTask);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTask);
            tvHora = itemView.findViewById(R.id.tvHoraTask);
            imgTask = itemView.findViewById(R.id.imgTask);
            cardTask = itemView.findViewById(R.id.cardTask);
            checkCompletada = itemView.findViewById(R.id.checkCompletada);
        }
    }
}
