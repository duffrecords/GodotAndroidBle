package com.duffrecords.godotandroidble;

import androidx.annotation.NonNull;

import com.welie.blessed.BluetoothBytesParser;

import java.io.Serializable;
import java.util.Locale;

import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT32;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT8;

public class CyclingCadenceMeasurement implements Serializable {

    public final Integer revs;
    public final Integer last;

    public CyclingCadenceMeasurement(byte[] value) {
        BluetoothBytesParser parser = new BluetoothBytesParser(value);

        // Parse the flags
        int flags = parser.getIntValue(FORMAT_UINT8);
        final int unit = flags & 0x01;

        // Parse cumulative revolutions
        this.revs = (unit == 0) ? parser.getIntValue(FORMAT_UINT16) : parser.getIntValue(FORMAT_UINT32);
        this.last = parser.getIntValue(FORMAT_UINT16);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "revs: %d\tlast: %d", revs, last);
    }
}
