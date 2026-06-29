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

        // LÓGICA DE CABECERA DE FECHA (SECCIÓN DE DÍAS)
        if (position == 0) {
            holder.tvHeaderFecha.setVisibility(View.VISIBLE);
            holder.tvHeaderFecha.setText(formatearFechaHeader(task.getFecha()));
        } else {
            Task anterior = listaTasks.get(position - 1);
            if (task.getFecha().equals(anterior.getFecha())) {
                holder.tvHeaderFecha.setVisibility(View.GONE);
            } else {
                holder.tvHeaderFecha.setVisibility(View.VISIBLE);
                holder.tvHeaderFecha.setText(formatearFechaHeader(task.getFecha()));
            }
        }

        holder.tvTitulo.setText(task.getTitulo());
        holder.tvDescripcion.setText(task.getDescripcion());
        holder.tvHora.setText(task.getHora());

        // SELECCIÓN DE PICTOGRAMA (HU 8)
        String picto = task.getPictograma();
        if (picto != null && !picto.isEmpty()) {
            // Intentar cargar como recurso primero
            int resId = context.getResources().getIdentifier(
                    picto,
                    "drawable",
                    context.getPackageName()
            );
            if (resId != 0) {
                holder.imgTask.setImageResource(resId);
            } else {
                // Si no es un recurso, buscar en la tabla de personalizados
                String rutaPersonalizada = sqLiteHelper.obtenerRutaPictograma(picto);
                if (rutaPersonalizada != null) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(rutaPersonalizada);
                    if (bitmap != null) {
                        holder.imgTask.setImageBitmap(bitmap);
                    } else {
                        asignarPictogramaAutomatico(holder, task.getTitulo().toLowerCase());
                    }
                } else {
                    asignarPictogramaAutomatico(holder, task.getTitulo().toLowerCase());
                }
            }
        } else {
            // Si no hay pictograma guardado, usamos la lógica automática
            asignarPictogramaAutomatico(holder, task.getTitulo().toLowerCase());
        }

        // checkbox de completado - RESETEAR LISTENER PARA EVITAR ERRORES DE RECICLAJE
        holder.checkCompletada.setOnCheckedChangeListener(null); 
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
                    
                    // Notificar al widget para que se actualice
                    Intent intentUpdate = new Intent(context, com.example.micalendario.widgets.NextTaskWidget.class);
                    intentUpdate.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int[] ids = android.appwidget.AppWidgetManager.getInstance(context)
                            .getAppWidgetIds(new android.content.ComponentName(context, com.example.micalendario.widgets.NextTaskWidget.class));
                    intentUpdate.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    context.sendBroadcast(intentUpdate);
                });

        // COLORES POR PERIODO (HU 1) - Siempre visibles para ayudar a la rutina
        String periodo = task.getPeriodo().toLowerCase();
        if (periodo.contains("mañana")) {
            holder.cardTask.setCardBackgroundColor(context.getColor(R.color.morning_color));
        } else if (periodo.contains("tarde")) {
            holder.cardTask.setCardBackgroundColor(context.getColor(R.color.afternoon_color));
        } else if (periodo.contains("noche")) {
            holder.cardTask.setCardBackgroundColor(context.getColor(R.color.night_color));
        } else {
            holder.cardTask.setCardBackgroundColor(context.getColor(R.color.surface));
        }

        // Colores de texto adaptativos
        holder.tvTitulo.setTextColor(context.getColor(R.color.text_primary));
        holder.tvDescripcion.setTextColor(context.getColor(R.color.text_secondary));
        holder.tvHora.setTextColor(context.getColor(R.color.text_primary));


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
        TextView tvHeaderFecha;
        ImageView imgTask;
        MaterialCardView cardTask;
        CheckBox checkCompletada;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloTask);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTask);
            tvHora = itemView.findViewById(R.id.tvHoraTask);
            tvHeaderFecha = itemView.findViewById(R.id.tvHeaderFecha);
            imgTask = itemView.findViewById(R.id.imgTask);
            cardTask = itemView.findViewById(R.id.cardTask);
            checkCompletada = itemView.findViewById(R.id.checkCompletada);
        }
    }

    private String formatearFechaHeader(String fecha) {
        try {
            java.text.SimpleDateFormat sdfInput = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.util.Date date = sdfInput.parse(fecha);
            java.text.SimpleDateFormat sdfOutput = new java.text.SimpleDateFormat("EEEE, d 'de' MMMM", new java.util.Locale("es", "ES"));
            String friendlyDate = sdfOutput.format(date);
            return friendlyDate.substring(0, 1).toUpperCase() + friendlyDate.substring(1);
        } catch (Exception e) {
            return fecha;
        }
    }
}
