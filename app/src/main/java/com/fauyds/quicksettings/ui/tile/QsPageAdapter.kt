package com.fauyds.quicksettings.ui.tile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fauyds.quicksettings.R
import com.fauyds.quicksettings.model.QsGridLayout
import com.fauyds.quicksettings.model.QsTileModel

class QsPageAdapter(
    private val onTileClick: (QsTileModel) -> Unit,
    private val onTileLongClick: (QsTileModel) -> Unit
) : RecyclerView.Adapter<QsPageAdapter.PageViewHolder>() {

    var pages: List<List<QsTileModel>> = emptyList()
    var gridLayout: QsGridLayout = QsGridLayout.DEFAULT
    var isDark: Boolean = true
    var activeSquareShape: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val recyclerView = RecyclerView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            setPadding(0, 4, 0, 4)
            clipToPadding = false
        }
        return PageViewHolder(recyclerView)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val pageTiles = pages.getOrNull(position) ?: emptyList()
        holder.bind(pageTiles, gridLayout, isDark, activeSquareShape, onTileClick, onTileLongClick)
    }

    override fun getItemCount(): Int = pages.size

    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView as RecyclerView

        fun bind(
            tiles: List<QsTileModel>,
            gridLayout: QsGridLayout,
            isDark: Boolean,
            activeSquareShape: Boolean,
            onTileClick: (QsTileModel) -> Unit,
            onTileLongClick: (QsTileModel) -> Unit
        ) {
            recyclerView.layoutManager = GridLayoutManager(itemView.context, gridLayout.columns)
            recyclerView.adapter = SinglePageGridAdapter(tiles, isDark, activeSquareShape, onTileClick, onTileLongClick)
        }
    }

    private class SinglePageGridAdapter(
        private val tiles: List<QsTileModel>,
        private val isDark: Boolean,
        private val activeSquareShape: Boolean,
        private val onTileClick: (QsTileModel) -> Unit,
        private val onTileLongClick: (QsTileModel) -> Unit
    ) : RecyclerView.Adapter<SinglePageGridAdapter.TileViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_qs_tile, parent, false)
            return TileViewHolder(view)
        }

        override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
            val tile = tiles.getOrNull(position) ?: return
            QsTileViewHelper.bindTile(
                view = holder.itemView,
                tile = tile,
                isDark = isDark,
                activeSquareShape = activeSquareShape,
                showSubtitle = true,
                tileIndex = position,
                onClick = onTileClick,
                onLongClick = onTileLongClick
            )
        }

        override fun getItemCount(): Int = tiles.size

        class TileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}
