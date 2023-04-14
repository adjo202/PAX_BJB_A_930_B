package com.pax.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class ListAdapter<T> extends BaseAdapter {

    private static final int DATA_CHANGE = 1 << 1;
    protected List<T> list;
    protected Context ctx;
    protected List<T> tempList; // 从本地添加的数据集合

    protected boolean loadFlag = true;
    /**
     * 加载过的图片
     */
    protected List<String> loadList = new ArrayList<>(0);

    protected Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_CHANGE:
                    notifyDataSetChanged();
                    break;
                default:
                    ListAdapter.this.handleMessage(msg);
                    break;
            }
        }

    };

    public ListAdapter(Context ctx) {
        this.ctx = ctx;

    }

    public void stopLoadImg() {
        loadFlag = false;
        notifyDataSetChanged();
    }

    public void resumeLoadImg() {
        loadFlag = true;
        notifyDataSetChanged();
    }

    /**
     * 异步提交 通知数据变更
     */
    public synchronized void postNotifyDataSetChanged() {
        handler.sendEmptyMessage(DATA_CHANGE);
    }

    protected void handleMessage(Message msg) {

    }

    /**
     * 自动添加数据至第一条
     * 
     * @param t
     */
    public void addFromLocal(T t) {
        if (null == list) {
            list = new ArrayList<>(0);
        }
        if (!list.contains(t))
            list.add(0, t);
    }

    public void add(T t) {
        if (null == list) {
            list = new ArrayList<>(0);
        }
        if (!list.contains(t))
            list.add(t);
    }

    public void add(T t, int index) {
        if (null == list) {
            list = new ArrayList<>(0);
        }
        if (!list.contains(t))
            list.add(index, t);
    }

    public void add(List<T> t) {
        if (null == list) {
            list = new ArrayList<>(0);
        }

        for (T item : t) {
            if (!list.contains(item))
                list.add(item);
        }
    }

    public void addBefore(List<T> t) {
        if (null == list) {
            list = new ArrayList<>(0);
        }
        if (null == t || t.size() < 1) {
            return;
        }

        List<T> tl = new ArrayList<>();
        for (T item : t) {
            if (list.contains(item)) {
                tl.add(item);
            }
        }
        list.removeAll(tl);
        list.addAll(0, t);

    }

    public void remove(int position) {
        if (list == null)
            return;
        if (position > -1 && position < this.getCount()) {
            list.remove(position);
        }
    }

    public void remove(T t) {
        if (list == null)
            return;
        if (list.contains(t)) {
            list.remove(t);
        }
    }

    public void setList(List<T> t) {
        this.list = t;
    }

    public List<T> getList() {
        return list;
    }

    public void clear() {
        if (null != list) {
            this.list.clear();
        }
        clearLoadList();
    }

    public void clearLoadList() {
        if (null != this.loadList) {
            this.loadList.clear();
        }
    }

    @Override
    public int getCount() {
        return null == list ? 0 : list.size();
    }

    public T getListItem(int position) {
        if (list == null)
            return null;
        if (position > -1 && position < this.getCount()) {
            return list.get(position);
        }
        return null;
    }

    @Override
    public Object getItem(int position) {
        if (list == null)
            return null;
        if (position > -1 && position < this.getCount()) {
            return list.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        T t = this.getListItem(position);
        if (null == convertView || convertView.getTag() == null) {
            convertView = View.inflate(ctx, getItemViewId(), null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        fillView(convertView, t, holder, position);

        return convertView;
    }

    /**
     * 创建ViewHolder
     * 
     * @param root
     * @return
     */
    protected abstract ViewHolder createViewHolder(View root);

    /**
     * 填充viewholder 数据
     * 
     * @param root
     * @param item
     * @param holder
     */
    protected abstract void fillView(View root, T item, ViewHolder holder, int position);

    /**
     * 子布局id
     * 
     * @return
     */
    protected abstract int getItemViewId();

    protected class ViewHolder {

    }

}
