package com.example.ui.tile

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.data.PanelPreferences
import com.example.model.QsTileModel
import com.example.model.TileCategory
import com.example.ui.notification.MonetThemeHelper
import java.util.Collections

enum class TileEditorFilter {
    ALL,
    QS_TILES,
    COMPLICATIONS
}

class TileEditAdapter(
    private val onDoneClick: () -> Unit,
    private val onTilesUpdated: (List<QsTileModel>, List<QsTileModel>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER_ACTIVE = 0
        const val VIEW_TYPE_FILTER_BAR = 1
        const val VIEW_TYPE_SECTION_HEADER = 2
        const val VIEW_TYPE_TILE_ACTIVE = 3
        const val VIEW_TYPE_TILE_INACTIVE = 4
        const val VIEW_TYPE_EMPTY_SECTION = 5
    }

    private sealed class EditorEntry {
        data object TopHeader : EditorEntry()
        data class SectionHeader(
            val id: String,
            val title: String,
            val hint: String,
            val isComplication: Boolean,
            val isAvailableSection: Boolean
        ) : EditorEntry()
        data class ActiveTile(val tile: QsTileModel) : EditorEntry()
        data class AvailableTile(val tile: QsTileModel) : EditorEntry()
        data class EmptySection(
            val message: String,
            val isComplication: Boolean,
            val isAvailableSection: Boolean
        ) : EditorEntry()
    }

    var activeTiles: MutableList<QsTileModel> = mutableListOf()
    var inactiveTiles: MutableList<QsTileModel> = mutableListOf()
    var isDark: Boolean = true
    var activeSquareShape: Boolean = false

    private var entries: List<EditorEntry> = emptyList()

    fun refreshDataSet() {
        rebuildEntries()
        notifyDataSetChanged()
    }

    private fun rebuildEntries() {
        val inactiveQs = inactiveTiles.filter { !it.isComplicationTile }
        val inactiveComp = inactiveTiles.filter { it.isComplicationTile }

        entries = buildList {
            add(EditorEntry.TopHeader)

            // 1. Active Tiles (Unified)
            add(
                EditorEntry.SectionHeader(
                    id = "active_tiles",
                    title = "ACTIVE TILES (${activeTiles.size})",
                    hint = "Hold & drag • Tap '-' to remove",
                    isComplication = false,
                    isAvailableSection = false
                )
            )
            if (activeTiles.isEmpty()) {
                add(
                    EditorEntry.EmptySection(
                        message = "No active tiles. Add one below.",
                        isComplication = false,
                        isAvailableSection = false
                    )
                )
            } else {
                activeTiles.forEach { add(EditorEntry.ActiveTile(it)) }
            }

            // 2. Available QS Tiles
            add(
                EditorEntry.SectionHeader(
                    id = "avail_qs",
                    title = "AVAILABLE QS TILES (${inactiveQs.size})",
                    hint = "Drag up to add • Tap '+'",
                    isComplication = false,
                    isAvailableSection = true
                )
            )
            if (inactiveQs.isEmpty()) {
                add(
                    EditorEntry.EmptySection(
                        message = "All QS tiles are currently in your active panel",
                        isComplication = false,
                        isAvailableSection = true
                    )
                )
            } else {
                inactiveQs.forEach { add(EditorEntry.AvailableTile(it)) }
            }

            // 3. Complications
            add(
                EditorEntry.SectionHeader(
                    id = "avail_comp",
                    title = "COMPLICATIONS (${inactiveComp.size})",
                    hint = "Drag up to add • Tap '+'",
                    isComplication = true,
                    isAvailableSection = true
                )
            )
            if (inactiveComp.isEmpty()) {
                add(
                    EditorEntry.EmptySection(
                        message = "All complications are currently in your active panel",
                        isComplication = true,
                        isAvailableSection = true
                    )
                )
            } else {
                inactiveComp.forEach { add(EditorEntry.AvailableTile(it)) }
            }
        }
    }

    fun isTilePosition(position: Int): Boolean {
        val entry = entries.getOrNull(position) ?: return false
        return entry is EditorEntry.ActiveTile || entry is EditorEntry.AvailableTile
    }

    fun getSpanSizeLookup(spanCount: Int): GridLayoutManager.SpanSizeLookup {
        return object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (getItemViewType(position)) {
                    VIEW_TYPE_HEADER_ACTIVE,
                    VIEW_TYPE_SECTION_HEADER,
                    VIEW_TYPE_EMPTY_SECTION -> spanCount
                    else -> 1
                }
            }
        }
    }

    override fun getItemCount(): Int = entries.size

    override fun getItemViewType(position: Int): Int {
        return when (entries.getOrNull(position)) {
            is EditorEntry.TopHeader -> VIEW_TYPE_HEADER_ACTIVE
            is EditorEntry.SectionHeader -> VIEW_TYPE_SECTION_HEADER
            is EditorEntry.ActiveTile -> VIEW_TYPE_TILE_ACTIVE
            is EditorEntry.AvailableTile -> VIEW_TYPE_TILE_INACTIVE
            is EditorEntry.EmptySection, null -> VIEW_TYPE_EMPTY_SECTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER_ACTIVE -> {
                val view = inflater.inflate(R.layout.item_tile_edit_header, parent, false)
                ActiveHeaderViewHolder(view)
            }
            VIEW_TYPE_SECTION_HEADER -> {
                val view = inflater.inflate(R.layout.item_tile_edit_divider, parent, false)
                SectionHeaderViewHolder(view)
            }
            VIEW_TYPE_EMPTY_SECTION -> {
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
        val entry = entries.getOrNull(position) ?: return

        val isKitKat = PanelPreferences.isAndroidKitKatStyle(context)
        val isMarshmallow = PanelPreferences.isAndroidMarshmallowStyle(context)
        val primaryAccent = when {
            isKitKat -> Color.parseColor("#33B5E5")
            isMarshmallow -> Color.parseColor("#00BFA5")
            else -> MonetThemeHelper.getPrimaryAccent(context, isDark)
        }
        val tertiaryAccent = when {
            isKitKat -> Color.parseColor("#33B5E5")
            isMarshmallow -> Color.parseColor("#00BFA5")
            else -> MonetThemeHelper.getTertiaryAccent(context, isDark)
        }
        val textSecondary = MonetThemeHelper.getTextSecondary(context, isDark)
        val onPrimary = if (isKitKat || isMarshmallow) Color.WHITE else MonetThemeHelper.getOnPrimaryText(context, isDark)

        when (holder) {
            is ActiveHeaderViewHolder -> {
                holder.tvTitle.text = if (isKitKat) "QUICK SETTINGS TILES" else if (isMarshmallow) "EDIT QUICK TILES" else "QUICK SETTINGS"
                holder.tvTitle.setTextColor(primaryAccent)
                holder.tvSubtitle.text = "Hold & drag tiles to reorder or move between pools"
                holder.tvSubtitle.setTextColor(textSecondary)
                holder.btnDone.visibility = View.VISIBLE
                val btnCorner = if (isKitKat) 4f * density else if (isMarshmallow) 8f * density else 16f * density
                holder.btnDone.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = btnCorner
                    setColor(primaryAccent)
                }
                holder.tvDoneText.setTextColor(onPrimary)
                holder.tvDoneText.text = if (isKitKat || isMarshmallow) "DONE" else "Done"
                holder.ivDoneIcon.setColorFilter(onPrimary)
                holder.btnDone.setOnClickListener { onDoneClick() }
            }

            is SectionHeaderViewHolder -> {
                val section = entry as? EditorEntry.SectionHeader ?: return
                val headerAccent = if (section.isComplication) tertiaryAccent else primaryAccent
                holder.tvTitle.text = section.title
                holder.tvTitle.setTextColor(headerAccent)
                holder.ivIcon.setImageResource(
                    if (section.isComplication) R.drawable.ic_notif_sparkles else R.drawable.ic_notif_tune
                )
                holder.ivIcon.setColorFilter(headerAccent)
                holder.tvHint.text = section.hint
                holder.tvHint.setTextColor(textSecondary)
            }
            is EmptyViewHolder -> {
                val empty = entry as? EditorEntry.EmptySection ?: return
                holder.tvEmpty.text = empty.message
                holder.tvEmpty.setTextColor(textSecondary)
            }
            is TileViewHolder -> {
                when (entry) {
                    is EditorEntry.ActiveTile -> {
                        val globalActiveIndex = activeTiles.indexOfFirst { it.id == entry.tile.id }
                        QsTileViewHelper.bindTile(
                            view = holder.itemView,
                            tile = entry.tile,
                            isDark = isDark,
                            activeSquareShape = activeSquareShape,
                            showSubtitle = false,
                            showBadge = true,
                            isBadgeRemove = true,
                            tileIndex = globalActiveIndex,
                            onBadgeClick = { removeTileById(entry.tile.id) },
                            onClick = { removeTileById(entry.tile.id) }
                        )
                    }
                    is EditorEntry.AvailableTile -> {
                        QsTileViewHelper.bindTile(
                            view = holder.itemView,
                            tile = entry.tile,
                            isDark = isDark,
                            activeSquareShape = activeSquareShape,
                            showSubtitle = false,
                            showBadge = true,
                            isBadgeRemove = false,
                            tileIndex = -1,
                            onBadgeClick = { addTileById(entry.tile.id) },
                            onClick = { addTileById(entry.tile.id) }
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    fun removeTileById(tileId: String) {
        val index = activeTiles.indexOfFirst { it.id == tileId }
        if (index in activeTiles.indices) {
            val removed = activeTiles.removeAt(index)
            inactiveTiles.removeAll { it.id == removed.id }
            inactiveTiles.add(0, removed.copy(isVisible = false))
            refreshDataSet()
            onTilesUpdated(activeTiles.toList(), inactiveTiles.toList())
        }
    }

    fun addTileById(tileId: String, targetActiveIndex: Int? = null) {
        val index = inactiveTiles.indexOfFirst { it.id == tileId }
        if (index in inactiveTiles.indices) {
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
            val insertAt = if (targetActiveIndex != null) {
                targetActiveIndex.coerceIn(0, activeTiles.size)
            } else {
                activeTiles.size
            }
            activeTiles.add(insertAt, activated)
            refreshDataSet()
            onTilesUpdated(activeTiles.toList(), inactiveTiles.toList())
        }
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val fromEntry = entries.getOrNull(fromPosition) ?: return false
        val toEntry = entries.getOrNull(toPosition) ?: return false

        // 1. Reorder within Active Tiles (Unified)
        if (fromEntry is EditorEntry.ActiveTile && toEntry is EditorEntry.ActiveTile) {
            val fromIdx = activeTiles.indexOfFirst { it.id == fromEntry.tile.id }
            val toIdx = activeTiles.indexOfFirst { it.id == toEntry.tile.id }
            if (fromIdx == -1 || toIdx == -1 || fromIdx == toIdx) return false

            val step = if (fromIdx < toIdx) 1 else -1
            for (i in fromIdx until toIdx step step) {
                Collections.swap(activeTiles, i, i + step)
            }
            rebuildEntries()
            notifyItemMoved(fromPosition, toPosition)
            return true
        }

        // 2. Reorder within same Available group
        if (fromEntry is EditorEntry.AvailableTile && toEntry is EditorEntry.AvailableTile) {
            if (fromEntry.tile.isComplicationTile != toEntry.tile.isComplicationTile) return false
            val isComp = fromEntry.tile.isComplicationTile
            val matching = inactiveTiles.filter { it.isComplicationTile == isComp }
            val fromGroupIdx = matching.indexOfFirst { it.id == fromEntry.tile.id }
            val toGroupIdx = matching.indexOfFirst { it.id == toEntry.tile.id }
            if (fromGroupIdx == -1 || toGroupIdx == -1 || fromGroupIdx == toGroupIdx) return false

            val step = if (fromGroupIdx < toGroupIdx) 1 else -1
            for (i in fromGroupIdx until toGroupIdx step step) {
                val aId = matching[i].id
                val bId = matching[i + step].id
                val aGlobal = inactiveTiles.indexOfFirst { it.id == aId }
                val bGlobal = inactiveTiles.indexOfFirst { it.id == bId }
                if (aGlobal != -1 && bGlobal != -1) {
                    Collections.swap(inactiveTiles, aGlobal, bGlobal)
                }
            }
            rebuildEntries()
            notifyItemMoved(fromPosition, toPosition)
            return true
        }

        // 3. Dragged from Active down into Available section
        if (fromEntry is EditorEntry.ActiveTile) {
            val isComp = fromEntry.tile.isComplicationTile
            val matchesAvailTarget = when (toEntry) {
                is EditorEntry.AvailableTile -> true
                is EditorEntry.SectionHeader -> toEntry.isAvailableSection
                is EditorEntry.EmptySection -> toEntry.isAvailableSection
                else -> false
            }
            if (matchesAvailTarget) {
                val removedIdx = activeTiles.indexOfFirst { it.id == fromEntry.tile.id }
                if (removedIdx != -1) {
                    val tile = activeTiles.removeAt(removedIdx)
                    val targetGlobalIdx = if (toEntry is EditorEntry.AvailableTile && toEntry.tile.isComplicationTile == isComp) {
                        inactiveTiles.indexOfFirst { it.id == toEntry.tile.id }.coerceAtLeast(0)
                    } else 0
                    inactiveTiles.add(targetGlobalIdx.coerceIn(0, inactiveTiles.size), tile.copy(isVisible = false))
                    refreshDataSet()
                    return true
                }
            }
        }

        // 4. Dragged from Available up into Active section
        if (fromEntry is EditorEntry.AvailableTile) {
            val matchesActiveTarget = when (toEntry) {
                is EditorEntry.ActiveTile -> true
                is EditorEntry.SectionHeader -> !toEntry.isAvailableSection
                is EditorEntry.EmptySection -> !toEntry.isAvailableSection
                else -> false
            }
            if (matchesActiveTarget) {
                val targetGlobalIdx = if (toEntry is EditorEntry.ActiveTile) {
                    activeTiles.indexOfFirst { it.id == toEntry.tile.id }.takeIf { it >= 0 }
                } else null
                addTileById(fromEntry.tile.id, targetGlobalIdx)
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



    class SectionHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_tile_edit_divider_title)
        val ivIcon: ImageView = view.findViewById(R.id.iv_tile_edit_divider_icon)
        val tvHint: TextView = view.findViewById(R.id.tv_tile_edit_divider_hint)
    }

    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmpty: TextView = view.findViewById(R.id.tv_tile_edit_empty_text)
    }

    class TileViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
