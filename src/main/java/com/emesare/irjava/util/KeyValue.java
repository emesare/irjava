package com.emesare.irjava.util;

import java.util.ArrayList;

public class KeyValue {
    private String key;
    private ArrayList<String> values;

    public KeyValue(String key, ArrayList<String> value) {
        this.key = key;
        this.values = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        if (values.size() > 0) {
            return values.get(0);
        } else {
            return "";
        }
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        String value = "";

        // TODO: Make this more efficient (better looking)
        for (int i = 0; i < values.size(); i++) {
            String currValue = values.get(i);
            if (i != values.size() - 1) {
                value = value.concat(currValue + ",");
            } else {
                value = value.concat(currValue); // Last does not need a delimiter.
            }
        }

        return key + "=" + value;
    }
}
