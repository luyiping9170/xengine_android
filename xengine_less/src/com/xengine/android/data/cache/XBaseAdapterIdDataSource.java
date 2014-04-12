package com.xengine.android.data.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 内存中的数据源。
 * 基于Id区分的数据存储。
 * Created by jasontujun.
 * Date: 11-12-17
 * Time: 上午1:01
 */
public abstract class XBaseAdapterIdDataSource<T> implements XAdapterDataSource<T>, XWithId<T> {

    /**
     * 实际的对象列表。
     */
    protected ArrayList<T> itemList = new ArrayList<T>();

    /**
     * 数据变化监听器
     */
    protected List<XDataChangeListener<T>> listeners = new ArrayList<XDataChangeListener<T>>();

    /**
     * 自动通知监听者
     */
    protected boolean isAutoNotify = true;

    @Override
    public void sort(Comparator<T> comparator) {
        Collections.sort(itemList, comparator);
    }

    @Override
    public T get(int index) {
        return itemList.get(index);
    }

    @Override
    public T getById(String id) {
        int index = getIndexById(id);
        if (index != -1)
            return get(index);
        else
            return null;
    }

    @Override
    public int size() {
        return itemList.size();
    }

    @Override
    public synchronized void add(T item) {
        if (item == null)
            return;

        int index = getIndexById(getId(item));
        if (index == -1) {
            itemList.add(item);
            if (isAutoNotify)
                notifyAddItem(item);
        } else {
            replace(index, item);
            if (isAutoNotify)
                notifyDataChanged();
        }
    }

    @Override
    public synchronized void addAll(List<T> items) {
        if (items == null || items.size() == 0)
            return;

        for (int i = 0; i<items.size(); i++) {
            T item = items.get(i);
            int index = getIndexById(getId(item));
            if (index == -1) {
                itemList.add(item);
            } else {
                replace(index, item);
            }
        }
        if (isAutoNotify)
            notifyAddItems(items);
    }

    @Override
    public boolean isEmpty() {
        return itemList.isEmpty();
    }

    @Override
    public synchronized void deleteById(String id) {
        int index = getIndexById(id);
        if (index != -1)
            delete(index);
    }

    @Override
    public synchronized void delete(int index) {
        if (index < 0 || index >= itemList.size())
            return;

        T item = itemList.remove(index);
        if (isAutoNotify)
            notifyDeleteItem(item);
    }

    @Override
    public synchronized void delete(T item) {
        if (itemList.remove(item)) {
            if (isAutoNotify)
                notifyDeleteItem(item);
        }
    }

    @Override
    public synchronized void deleteAll(List<T> items) {
        if (itemList.removeAll(items)) {
            if (isAutoNotify)
                notifyDeleteItems(items);
        }
    }

    @Override
    public synchronized void deleteAllById(List<String> ids) {
        if (ids == null || ids.size() == 0)
            return;
        List<T> items = new ArrayList<T>();
        for (int i = 0; i < ids.size(); i++) {
            T item = getById(ids.get(i));
            if (item != null) {
                items.add(item);
            }
        }
        deleteAll(items);
    }

    @Override
    public int indexOf(T item) {
        return itemList.indexOf(item);
    }

    @Override
    public boolean contains(T item) {
        if (item == null)
            return false;

        final String id = getId(item);
        for (int i = 0; i<size(); i++) {
            T tmp = get(i);
            if (getId(tmp).equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回数据源中所有的数据项的副本
     */
    @Override
    public List<T> copyAll() {
        return new ArrayList<T>(itemList);
    }

    @Override
    public synchronized void clear() {
        List<T> copyItems = new ArrayList<T>(itemList);
        itemList.clear();
        if (isAutoNotify)
            notifyDeleteItems(copyItems);
    }

    @Override
    public synchronized void registerDataChangeListener(XDataChangeListener<T> listener) {
        if (listener != null) {
            // cow
            List<XDataChangeListener<T>> copyListeners = new ArrayList<XDataChangeListener<T>>(listeners);
            if (!copyListeners.contains(listener)) {
                copyListeners.add(listener);
                listeners = copyListeners;
            }
        }
    }

    @Override
    public synchronized void unregisterDataChangeListener(XDataChangeListener<T> listener) {
        if (listener != null) {
            // cow
            List<XDataChangeListener<T>> copyListeners = new ArrayList<XDataChangeListener<T>>(listeners);
            if (copyListeners.remove(listener))
                listeners = copyListeners;
        }
    }

    @Override
    public void notifyDataChanged() {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners) {
            listener.onChange();
        }
    }

    protected void notifyAddItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAdd(item);
    }

    protected void notifyAddItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAddAll(items);
    }

    protected void notifyDeleteItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDelete(item);
    }

    protected void notifyDeleteItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDeleteAll(items);
    }

    /**
     * 替换某一项（用于重复添加的情况）
     * TIP 子类可覆盖此方法
     * @param index
     * @param newItem
     */
    @Override
    public void replace(int index, T newItem) {
        itemList.set(index, newItem);
    }

    @Override
    public int getIndexById(String id) {
        for (int i = 0; i<size(); i++) {
            T tmp = get(i);
            if (getId(tmp).equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setAutoNotifyListeners(boolean isAuto) {
        this.isAutoNotify = isAuto;
    }
}
