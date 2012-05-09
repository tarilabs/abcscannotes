package net.tarilabs.abcscannotes;

import android.text.InputType;
import android.text.method.DigitsKeyListener;

/**
 * A class to implement a simple keyboard input with only digits, for custom keyboard on desired input fields
 * 
 * @author mmortari
 *
 */
public class CustomDigitsKeyListener extends DigitsKeyListener {
    public CustomDigitsKeyListener() {
        super(false, false);
    }

    public CustomDigitsKeyListener(boolean sign, boolean decimal) {
        super(sign, decimal);
    }

    public int getInputType() {
        return InputType.TYPE_CLASS_PHONE;
    }
}
