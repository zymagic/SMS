package rex.sms.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by zy on 2018/3/23.
 */
abstract class RecyclerAdapter<T>(context: Context) : RecyclerView.Adapter<RecyclerViewHolder<T>>() {

    private val inflater = LayoutInflater.from(context)
    private val items = ArrayList<T>()

    abstract fun getLayoutResource(): Int
    abstract fun onCreatePresenter() : RecyclerPresenter<T>

    fun addItem(item: T) {
        items.add(item)
        notifyDataSetChanged()
    }

    fun removeItem(item: T) {
        val index = items.indexOf(item)
        if (index >= 0) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder<T> {
        val view = inflater.inflate(getLayoutResource(), parent, false)
        val presenter = onCreatePresenter()
        presenter.create(view)
        return RecyclerViewHolder(view, presenter)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder<T>, position: Int) {
        holder.presenter.position = position
        holder.presenter.bind(items[position])
    }

}

class RecyclerViewHolder<T>(view: View, val presenter: RecyclerPresenter<T>) : RecyclerView.ViewHolder(view)

abstract class RecyclerPresenter<T> {
    var position = -1
    var model: T? = null
    lateinit var view: View

    fun create(view: View) {
        this.view = view
        onCreate(view)
    }

    fun bind(model: T) {
        this.model = model
        onBind(model)
    }

    abstract fun onCreate(view: View)
    abstract fun onBind(model: T)
}
