package com.kedo.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kedo.app.R
import com.kedo.app.domain.Evento

class EventoAdapter(private var eventos: List<Evento>) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    // 1. El ViewHolder busca y guarda las referencias a los TextView de tu tarjeta XML
    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloEvento)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionEvento)
    }

    // 2. Aquí le decimos qué diseño XML debe "inflar" (el item_evento que acabas de crear)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    // 3. Devuelve cuántos eventos hay en total en la lista
    override fun getItemCount(): Int = eventos.size

    // 4. Aquí ocurre la magia: metemos los datos reales del evento en la tarjeta visual
    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.tvTitulo.text = evento.titulo
        holder.tvDescripcion.text = evento.descripcion
    }

    // Función extra que usaremos para actualizar la lista cuando lleguen datos nuevos de internet
    fun actualizarEventos(nuevaLista: List<Evento>) {
        eventos = nuevaLista
        notifyDataSetChanged() // Avisa a Android para que redibuje la pantalla
    }
}