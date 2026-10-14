package com.example.ui.tile

import android.content.Context
import android.graphics.Color
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.model.QsTileModel
import com.example.model.TileCategory
import com.example.ui.notification.MonetThemeHelper
import java.util.Collections

class TileEditAdapter(
    private val onDoneClick: () -> Unit,
    private val onTilesUpdated: (List<QsTileModel>, List<QsTileModel>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER_ACTIVE = 0
        const val VIEW_TYPE_TILE_ACTIVE = 1
        const val VIEW_TYPE_HEADER_INACTIVE = 2
        const val VIEW_TYPE_TILE_INACTIVE = 3
        const val VIEW_TYPE_EMPTY_INACTIVE = 4
    }

    var activeTiles: MutableList<QsTileModel> = mutableListOf()
    var inactiveTiles: MutableList<QsTileModel> = mutableListOf()
    var isDark: Boolean = true
    var activeSquareShape: Boolean = false

    fun getSpanSizeLookup(spanCount: Int): GridLayoutManager.SpanSizeLookup {
        return object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (getItemViewType(position)) {
                    VIEW_TYPE_HEADER_ACTIVE,
                    VIEW_TYPE_HEADER_INACTIVE,
                    VIEW_TYPE_EMPTY_INACTIVE -> spanCount
                    else -> 1
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val activeCount = 1 + activeTiles.size // Header + Active Tiles
        val inactiveCount = 1 + if (inactiveTiles.isEmpty()) 1 else inactiveTiles.size // Header + (Tiles or Empty)
        return activeCount + inactiveCount
    }

    override fun getItemViewType(position: Int): Int {
        val activeHeaderPos = 0
        val lastActivePos = activeTiles.size // 1 to activeTiles.size
        val inactiveHeaderPos = activeTiles.size + 1

        return when {
            position == activeHeaderPos -> VIEW_TYPE_HEADER_ACTIVE
            position in 1..lastActivePos -> VIEW_TYPE_TILE_ACTIVE
            position == inactiveHeaderPos -> VIEW_TYPE_HEADER_INACTIVE
            inactiveTiles.isEmpty() && position == inactiveHeaderPos + 1 -> VIEW_TYPE_EMPTY_INACTIVE
            else -> VIEW_TYPE_TILE_INACTIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER_ACTIVE -> {
                val view = inflater.inflate(R.layout.item_tile_edit_header, parent, false)
                ActiveHeaderViewHolder(view)
            }
            VIEW_TYPE_HEADER_INACTIVE -> {
                val view = inflater.inflate(R.layout.item_tile_edit_divider, parent, false)
                InactiveHeaderViewHolder(view)
            }
            VIEW_TYPE_EMPTY_INACTIVE -> {
                val view = inflater.inflate(R.layout.item_tile_edit_empty, parent, false)
                EmptyViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_qs_tile, parent, false)
                TileViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        val density = context.resources.displayMetrics.density

        when (holder) {
            is ActiveHeaderViewHolder -> {
                val primaryAccent = MonetThemeHelper.getPrimaryAccent(context, isDark)
                val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)
                val onPrimary = MonetThemeHelper.getOnPrimaryText(context, isDark)

                holder.tvTitle.setTextColor(primaryAccent)
                holder.tvSubtitle.setTextColor(textSecondary)
                holder.btnDone.visibility = android.view.View.VISIBLE
                holder.btnDone.background = MonetThemeHelper.createMonetPrimaryPillDrawable(context, isDark, 16f * density)
                holder.tvDoneText.setTextColor(onPrimary)
                holder.ivDoneIcon.setColorFilter(onPrimary)
            }
            is InactiveHeaderViewHolder -> {
                val primaryAccent = MonetThemeHelper.getPrimaryAccent(context, isDark)
                val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)

                holder.tvTitle.text = "AVAILABLE TILES (${inactiveTiles.size})"
                holder.tvTitle.setTextColor(primaryAccent)
                holder.ivIcon.setColorFilter(primaryAccent)
                holder.tvHint.setTextColor(textSecondary)
            }
            is EmptyViewHolder -> {
                val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)
                holder.tvEmpty.setTextColor(textSecondary)
            }
            is TileViewHolder -> {
                val viewType = getItemViewType(position)
                if (viewType == VIEW_TYPE_TILE_ACTIVE) {
                    val activeIndex = position - 1
                    val tile = activeTiles.getOrNull(activeIndex) ?: return
                    QsTileViewHelper.bindTile(
                        view = holder.itemView,
                        tile = tile,
                        isDark = isDark,
                        activeSquareShape = activeSquareShape,
                        showSubtitle = false,
                        showBadge = true,
                        isBadgeRemove = true,
                        tileIndex = activeIndex,
                        onBadgeClick = {
                            val pos = holder.bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                removeTileAt(pos - 1)
                            }
                        },
                        onClick = {
                            val pos = holder.bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                removeTileAt(pos - 1)
                            }
                        }
                    )
                } else if (viewType == VIEW_TYPE_TILE_INACTIVE) {
                    val inactiveIndex = position - (activeTiles.size + 2)
                    val tile = inactiveTiles.getOrNull(inactiveIndex) ?: return
                    QsTileViewHelper.bindTile(
                        view = holder.itemView,
                        tile = tile,
                        isDark = isDark,
                        activeSquareShape = activeSquareShape,
                        showSubtitle = false,
                        showBadge = true,
                        isBadgeRemove = false,
                        tileIndex = -1,
                        onBadgeClick = {
                            val pos = holder.bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                addTileAt(pos - (activeTiles.size + 2))
                            }
                        },
                        onClick = {
                            val pos = holder.bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                addTileAt(pos - (activeTiles.size + 2))
                            }
                        }
                    )
                }
            }
        }
    }

    fun removeTileAt(index: Int) {
        if (index in 0 until activeTiles.size) {
            val removed = activeTiles.removeAt(index)
            inactiveTiles.add(0, removed.copy(isVisible = false))
            notifyDataSetChanged()
            onTilesUpdated(activeTiles.toList(), inactiveTiles.toList())
        }
    }

    fun addTileAt(index: Int) {
        if (index in 0 until inactiveTiles.size) {
            val moved = inactiveTiles.removeAt(index)
            val isExt = moved.isExternal || moved.category == TileCategory.THIRD_PARTY || moved.category == TileCategory.EXTERNAL || moved.id.contains("/")
            val pkg = moved.packageName ?: if (moved.id.contains("/")) moved.id.substringBefore('/') else null
            val cls = moved.serviceClassName ?: if (moved.id.contains("/")) moved.id.substringAfter('/') else null
            val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else moved.id

            val activated = moved.copy(
                id = compId,
                isVisible = true,
                isExternal = isExt,
                category = if (isExt) TileCategory.THIRD_PARTY else moved.category,
                packageName = pkg,
                serviceClassName = cls
            )
            activeTiles.add(activated)
            notifyDataSetChanged()
            onTilesUpdated(activeTiles.toList(), inactiveTiles.toList())
        }
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val fromType = getItemViewType(fromPosition)
        val toType = getItemViewType(toPosition)

        // Cannot drop directly on headers or empty placeholder view
        if (toType == VIEW_TYPE_HEADER_ACTIVE || toType == VIEW_TYPE_HEADER_INACTIVE || toType == VIEW_TYPE_EMPTY_INACTIVE) {
            return false
        }

        // 1. Moving within Active section
        if (fromType == VIEW_TYPE_TILE_ACTIVE && toType == VIEW_TYPE_TILE_ACTIVE) {
            val fromIndex = fromPosition - 1
            val toIndex = toPosition - 1
            if (fromIndex in 0 until activeTiles.size && toIndex in 0 until activeTiles.size) {
                Collections.swap(activeTiles, fromIndex, toIndex)
                notifyItemMoved(fromPosition, toPosition)
                onTilesUpdated(activeTiles.toList(), inactiveTiles.toList())
                return true
            }
        }

        // 2. Moving within Inactive section
        if (fromType == VIEW_TYPE_TILE_INACTIVE && toType == VIEW_TYPE_TILE_INACTIVE) {
            val fromIndex = fromPosition - (activeTiles.size + 2)
            val toIndex = toPosition - (activeTiles.size + 2)
            if (fromIndex in 0 until inactiveTiles.size && toIndex in 0 until inactiveTiles.size) {
                Collections.swap(inactiveTiles, fromIndex, toIndex)
                notifyItemMoved(fromPosition, toPosition)
                onTilesUpdated(activeTiles.toList(), inactiveTiles.toList())
                return true
            }
        }

        // 3. Dragged from Active down into Inactive section
        if (fromType == VIEW_TYPE_TILE_ACTIVE && toType == VIEW_TYPE_TILE_INACTIVE) {
            val fromIndex = fromPosition - 1
            val toIndex = (toPosition - (activeTiles.size + 2)).coerceIn(0, inactiveTiles.size)
            if (fromIndex in 0 until activeTiles.size) {
                val tile = activeTiles.removeAt(fromIndex)
                inactiveTiles.add(toIndex, tile.copy(isVisible = false))
                notifyDataSetChanged()
                onTilesUpdated(activeTiles.toList(), inactiveTiles.toList())
                return true
            }
        }

        // 4. Dragged from Inactive up into Active section
        if (fromType == VIEW_TYPE_TILE_INACTIVE && toType == VIEW_TYPE_TILE_ACTIVE) {
            val fromIndex = fromPosition - (activeTiles.size + 2)
            val toIndex = (toPosition - 1).coerceIn(0, activeTiles.size)
            if (fromIndex in 0 until inactiveTiles.size) {
                val tile = inactiveTiles.removeAt(fromIndex)
                activeTiles.add(toIndex, tile.copy(isVisible = true))
                notifyDataSetChanged()
                onTilesUpdated(activeTiles.toList(), inactiveTiles.toList())
                return true
            }
        }

        return false
    }

    class ActiveHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_tile_edit_header_title)
        val tvSubtitle: TextView = view.findViewById(R.id.tv_tile_edit_header_subtitle)
        val btnDone: LinearLayout = view.findViewById(R.id.btn_tile_edit_header_done)
        val ivDoneIcon: ImageView = view.findViewById(R.id.iv_tile_edit_header_done_icon)
        val tvDoneText: TextView = view.findViewById(R.id.tv_tile_edit_header_done_text)
    }

    class InactiveHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_tile_edit_divider_title)
        val ivIcon: ImageView = view.findViewById(R.id.iv_tile_edit_divider_icon)
        val tvHint: TextView = view.findViewById(R.id.tv_tile_edit_divider_hint)
    }

    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmpty: TextView = view.findViewById(R.id.tv_tile_edit_empty_text)
    }

    class TileViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
