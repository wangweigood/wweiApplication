package com.smartwebee.android.blespp;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by uidp5437 on 2017/8/9.
 */

public class ReceivedDataFromBluetooth implements Serializable {
    private Date date;
    private String mData;
    private boolean m_FlagEvent = false;
    private byte m_TrameType = 0;

    public ReceivedDataFromBluetooth(Date date, String mData) {
        this.date = date;
        this.mData = mData;
    }

    @Override
    public String toString() {
        return date + ":" + mData + " \n";
    }
}
