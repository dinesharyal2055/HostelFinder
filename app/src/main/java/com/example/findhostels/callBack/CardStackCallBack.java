package com.example.findhostels.callBack;

import androidx.recyclerview.widget.DiffUtil;

import com.example.findhostels.models.Hostel;

import java.util.List;

public class CardStackCallBack extends DiffUtil.Callback {
    private List<Hostel> old, baru;

    public CardStackCallBack(List<Hostel> old, List<Hostel> baru) {
        this.old = old;
        this.baru = baru;
    }

    @Override
    public int getOldListSize() {
        return old.size();
    }

    @Override
    public int getNewListSize() {
        return baru.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return old.get(oldItemPosition).getImgUrl() == baru.get(newItemPosition).getImgUrl();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return old.get(oldItemPosition) == baru.get(newItemPosition);
    }
}
