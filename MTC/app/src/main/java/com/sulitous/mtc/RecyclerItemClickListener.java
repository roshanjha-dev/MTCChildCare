package com.sulitous.mtc;

import android.view.View;

interface RecyclerItemClickListener {

    void onClick(View view, int position, boolean isLongClick);

}