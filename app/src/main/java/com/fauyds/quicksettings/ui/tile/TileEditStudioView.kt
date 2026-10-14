package com.fauyds.quicksettings.ui.tile

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.fauyds.quicksettings.R
import com.fauyds.quicksettings.data.PanelPreferences
import com.fauyds.quicksettings.data.local.QuickPanelDatabase
import com.fauyds.quicksettings.data.local.TileEntity
import com.fauyds.quicksettings.data.repository.TileRepository
import com.fauyds.quicksettings.model.QsGridLayout
import com.fauyds.quicksettings.model.QsTileModel
import com.fauyds.quicksettings.model.TileCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TileEditStudioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val rvTiles: RecyclerView
    private var adapter: TileEditAdapter? = null

    var onTilesUpdated: ((List<QsTileModel>, List<QsTileModel>) -> Unit)? = null
    var onDone: (() -> Unit)? = null

    private var currentColumns: Int = 4

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.layout_tile_edit_studio, this, true)
        rvTiles = root.findViewById(R.id.rv_tile_edit_studio)

        val gridLayoutManager = GridLayoutManager(context, currentColumns)
        rvTiles.layoutManager = gridLayoutManager

        rvTiles.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.actionMasked == MotionEvent.ACTION_DOWN) {
                    rv.parent?.requestDisallowInterceptTouchEvent(true)
                }
                return false
            }
        })

        initAdapter()
    }

    private fun initAdapter() {
        adapter = TileEditAdapter(
            onDoneClick = {
                onDone?.invoke()
            },
            onTilesUpdated = { activeList, inactiveList ->
                onTilesUpdated?.invoke(activeList, inactiveList)
                persistTiles(activeList, inactiveList)
            }
        )
        (rvTiles.layoutManager as? GridLayoutManager)?.spanSizeLookup = adapter?.getSpanSizeLookup(currentColumns)
        rvTiles.adapter = adapter

        // Setup smooth native ItemTouchHelper for UP / DOWN / LEFT / RIGHT drag reordering
        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) return false
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                return adapter?.onItemMove(fromPos, toPos) ?: false
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    viewHolder?.itemView?.alpha = 0.85f
                    viewHolder?.itemView?.scaleX = 1.08f
                    viewHolder?.itemView?.scaleY = 1.08f
                    viewHolder?.itemView?.translationZ = 12f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
                viewHolder.itemView.scaleX = 1.0f
                viewHolder.itemView.scaleY = 1.0f
                viewHolder.itemView.translationZ = 0f
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                adapter?.let {
                    persistTiles(it.activeTiles.toList(), it.inactiveTiles.toList())
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        touchHelper.attachToRecyclerView(rvTiles)
    }

    private fun persistTiles(activeList: List<QsTileModel>, inactiveList: List<QsTileModel>) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = QuickPanelDatabase.getInstance(appContext)
                val repo = TileRepository(db.tileDao())
                val entities = mutableListOf<TileEntity>()
                var order = 0

                for (tile in activeList) {
                    val isExt = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else tile.id

                    entities.add(
                        TileEntity(
                            tileId = if (isExt) compId else tile.id,
                            label = tile.label,
                            subtitle = tile.subtitle,
                            categoryName = if (isExt) TileCategory.THIRD_PARTY.name else tile.category.name,
                            orderIndex = order++,
                            isVisible = true,
                            isQqsFavorite = tile.isQqsFavorite,
                            isExternal = isExt,
                            packageName = pkg,
                            serviceClassName = cls,
                            settingsAction = tile.settingsAction
                        )
                    )
                }

                for (tile in inactiveList) {
                    val isExt = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else tile.id

                    entities.add(
                        TileEntity(
                            tileId = if (isExt) compId else tile.id,
                            label = tile.label,
                            subtitle = tile.subtitle,
                            categoryName = if (isExt) TileCategory.THIRD_PARTY.name else tile.category.name,
                            orderIndex = order++,
                            isVisible = false,
                            isQqsFavorite = tile.isQqsFavorite,
                            isExternal = isExt,
                            packageName = pkg,
                            serviceClassName = cls,
                            settingsAction = tile.settingsAction
                        )
                    )
                }

                repo.insertAll(entities)
                PanelPreferences.saveActiveTileIds(appContext, activeList.map { it.id })
            } catch (_: Throwable) {}
        }
    }

    fun updateData(
        activeTiles: List<QsTileModel>,
        inactiveTiles: List<QsTileModel>,
        gridLayout: QsGridLayout,
        activeSquareShape: Boolean,
        isDark: Boolean
    ) {
        val columns = gridLayout.columns.coerceIn(2, 6)
        if (currentColumns != columns || (rvTiles.layoutManager as? GridLayoutManager)?.spanCount != columns) {
            currentColumns = columns
            val gridLayoutManager = GridLayoutManager(context, columns)
            gridLayoutManager.spanSizeLookup = adapter?.getSpanSizeLookup(columns) ?: object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) = 1
            }
            rvTiles.layoutManager = gridLayoutManager
        }

        adapter?.activeTiles = activeTiles.toMutableList()
        adapter?.inactiveTiles = inactiveTiles.toMutableList()
        adapter?.isDark = isDark
        adapter?.activeSquareShape = activeSquareShape
        adapter?.notifyDataSetChanged()
    }
}
