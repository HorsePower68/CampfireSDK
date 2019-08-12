package com.sayzen.campfiresdk.views

import android.graphics.Bitmap
import android.provider.MediaStore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.GridLayoutManager
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.dialogs.DialogSheetWidget
import com.sup.dev.android.views.dialogs.DialogWidget
import com.sup.dev.android.views.widgets.WidgetProgressWithTitle
import com.sup.dev.android.views.widgets.WidgetRecycler
import com.sup.dev.java.classes.items.Item
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsFiles
import com.sup.dev.java.tools.ToolsNetwork
import com.sup.dev.java.tools.ToolsThreads
import java.io.File

open class WidgetStickers : WidgetRecycler(R.layout.widget_stickers) {

    private val DP = ToolsView.dpToPx(1).toInt()
    private val myAdapter: RecyclerCardAdapter = RecyclerCardAdapter()
    private val vEmptyText: TextView = findViewById(R.id.vEmptyText)
    private val vFabGalleryContainer: View = ToolsView.inflate(R.layout.z_fab)
    private val vFabLinkContainer: View = ToolsView.inflate(R.layout.z_fab)
    private val vFabDoneContainer: View = ToolsView.inflate(R.layout.z_fab)
    private val vFabGallery: ImageView = vFabGalleryContainer.findViewById(R.id.vFab)
    private val vFabLink: ImageView = vFabLinkContainer.findViewById(R.id.vFab)
    private val vFabDone: FloatingActionButton = vFabDoneContainer.findViewById(R.id.vFab)

    private var onSelected: (WidgetStickers, ByteArray, Int) -> Unit = { widget, bytes, index -> }
    private var imagesLoaded: Boolean = false
    private var spanCount = 3
    private var maxSelectCount = 1
    private var selectedList = ArrayList<File>()
    private var callbackInWorkerThread = false
    private var hided = false
    private val addedHash = SparseArray<Boolean>()

    init {
        vEmptyText.text = SupAndroid.TEXT_ERROR_CANT_FIND_IMAGES


        spanCount = if (ToolsAndroid.isScreenPortrait()) 3 else 6
        vRecycler.layoutManager = GridLayoutManager(view.context, spanCount)
        ToolsView.setRecyclerAnimation(vRecycler)

        vFabGallery.setImageResource(R.drawable.ic_landscape_white_24dp)
        vFabLink.setImageResource(R.drawable.ic_insert_link_white_24dp)
        vFabDone.setImageResource(R.drawable.ic_done_white_24dp)
        vFabGallery.setOnClickListener { }
        vFabLink.setOnClickListener { }
        vFabDone.setOnClickListener { sendAll() }

        ToolsView.setFabColorR(vFabDone, R.color.green_700)

        setAdapter<WidgetRecycler>(myAdapter)
        updateFabs()

        ToolsThreads.timerMain(4000) {
            if (!imagesLoaded) return@timerMain
            if (hided) it.unsubscribe()
            else loadImagesNow()
        }
    }

    override fun onHide() {
        super.onHide()
        hided = true
    }

    fun updateFabs() {
        if (selectedList.isEmpty()) {
            vContainer.removeView(vFabDoneContainer)
            if (vContainer.indexOfChild(vFabGalleryContainer) == -1) vContainer.addView(vFabGalleryContainer)
            if (vContainer.indexOfChild(vFabLinkContainer) == -1) vContainer.addView(vFabLinkContainer)
            (vFabLinkContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(72).toInt()
        } else {
            vContainer.removeView(vFabGalleryContainer)
            vContainer.removeView(vFabLinkContainer)
            if (vContainer.indexOfChild(vFabDoneContainer) == -1) vContainer.addView(vFabDoneContainer)
        }
    }

    override fun onShow() {
        super.onShow()
        loadImages()

        (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        vRecycler.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        if (viewWrapper is DialogWidget)
            (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(2).toInt(), ToolsView.dpToPx(8).toInt(), 0)
        else if (viewWrapper is DialogSheetWidget)
            vRecycler.layoutParams.height = ToolsView.dpToPx(320).toInt()
    }

    private fun loadImages() {
        if (imagesLoaded) return

        ToolsPermission.requestReadPermission({
            imagesLoaded = true
            loadImagesNow()
        }, {
            ToolsToast.show(SupAndroid.TEXT_ERROR_PERMISSION_FILES)
            hide()
        })

    }

    private fun loadImagesNow() {

        val offset = myAdapter.size()
        var addCount = 0
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
        val cursor = SupAndroid.appContext!!.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC")

        while (cursor!!.moveToNext()) {
            val file = File(cursor.getString(0))
            val hash = file.hashCode()

            if (addedHash.get(hash) != null) break

            addCount++
            addedHash.put(hash, true)
            myAdapter.add(myAdapter.size() - offset, CardImage(file))
        }

        if (addCount > 0 && offset > 0) {
            vRecycler.scrollToPosition(0)
        }

        vEmptyText.visibility = if (myAdapter.isEmpty) View.VISIBLE else View.GONE
    }

    private fun loadLink(link: String) {
        val progress = ToolsView.showProgressDialog()
        ToolsNetwork.getBytesFromURL(link, onResult = {
            progress.hideForce()
            if (it == null || !ToolsBytes.isImage(it)) ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
            else {
                if(callbackInWorkerThread) ToolsThreads.thread {onSelected.invoke(this, it, 0)  }
                else onSelected.invoke(this, it, 0)
                hide()
            }
        })
    }

    private fun sendAll() {
        val d = if (selectedList.size > 1 && SupAndroid.TEXT_APP_LOADING != null) ToolsView.showProgressDialog(SupAndroid.TEXT_APP_LOADING + " 1 / " + selectedList.size) else ToolsView.showProgressDialog()
        ToolsThreads.thread {
            for (f in selectedList) {
                if (d is WidgetProgressWithTitle) ToolsThreads.main { d.setTitle(SupAndroid.TEXT_APP_LOADING + " ${selectedList.indexOf(f) + 1} / " + selectedList.size) }
                try {

                    val bytes = ToolsFiles.readFile(f)

                    if (callbackInWorkerThread) {
                        onSelected.invoke(this, bytes, selectedList.indexOf(f))
                    } else {
                        val sent = Item(false)
                        ToolsThreads.main {
                            try {
                                onSelected.invoke(this, bytes, selectedList.indexOf(f))
                            } catch (e: Exception) {
                                err(e)
                            }
                            sent.a = true
                        }
                        while (!sent.a) ToolsThreads.sleep(10)
                    }

                } catch (e: Exception) {
                    err(e)
                    ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
                }
            }
            ToolsThreads.main {
                hide()
                d.hide()
            }
        }
    }

    //
    //  Setters
    //

    fun setOnSelected(onSelected: (WidgetStickers, ByteArray, Int) -> Unit): WidgetStickers {
        this.onSelected = onSelected
        return this
    }

    fun setOnSelectedBitmap(callback: (WidgetStickers, Bitmap) -> Unit): WidgetStickers {
        this.onSelected = { w, bytes, index -> callback.invoke(this, ToolsBitmap.decode(bytes)!!) }
        return this
    }

    fun setMaxSelectCount(maxSelectCount: Int): WidgetStickers {
        this.maxSelectCount = maxSelectCount
        return this
    }

    fun setCallbackInWorkerThread(callbackInWorkerThread: Boolean): WidgetStickers {
        this.callbackInWorkerThread = callbackInWorkerThread
        return this
    }

    //
    //  Card
    //

    private inner class CardImage(val file: File) : Card() {

        override fun getLayout() = R.layout.sheet_choose_image_card

        override fun bindView(view: View) {
            super.bindView(view)
            val vImage: ImageView = view.findViewById(R.id.vImage)
            val vNumContainerTouch: View = view.findViewById(R.id.vNumContainerTouch)

            vImage.setOnClickListener { v -> onClick() }
            if (maxSelectCount > 1) {
                vImage.setOnLongClickListener { v ->
                    onLongClick()
                    true
                }
                vNumContainerTouch.setOnClickListener { onLongClick() }
            }

            ToolsImagesLoader.load(file).size(420, 420).cropSquare().into(vImage)
            val index = adapter!!.indexOf(this)
            val arg = index % spanCount
            view.setPadding(if (arg == 0) 0 else DP, if (index < spanCount) 0 else DP, if (arg == spanCount - 1) 0 else DP, DP)

            updateIndex()
        }

        fun updateIndex() {
            val view = getView()
            if (view == null) return
            val vNum: TextView = view.findViewById(R.id.vNum)
            val vNumContainer: View = view.findViewById(R.id.vNumContainer)

            val selectIndex = selectedList.indexOf(file)
            vNum.text = if (selectIndex == -1) "   " else " ${selectIndex + 1} "
            vNumContainer.visibility = if (maxSelectCount > 1) View.VISIBLE else View.GONE
            vNumContainer.setBackgroundColor(if (selectIndex == -1) ToolsResources.getColor(R.color.focus_dark) else ToolsResources.getAccentColor(context))
        }

        fun onClick() {
            if (selectedList.isNotEmpty()) {
                onLongClick()
            } else {
                selectedList.add(file)
                sendAll()
            }
        }

        fun onLongClick() {
            if (selectedList.contains(file)) {
                selectedList.remove(file)
                updateFabs()
                for (c in myAdapter.get(CardImage::class)) c.updateIndex()
            } else {
                if (selectedList.size >= maxSelectCount) {
                    if (SupAndroid.TEXT_ERROR_MAX_ITEMS_COUNT != null) ToolsToast.show(SupAndroid.TEXT_ERROR_MAX_ITEMS_COUNT)
                    return
                }
                selectedList.add(file)
                updateFabs()
                update()
            }
        }


    }

}
