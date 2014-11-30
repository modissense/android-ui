package gr.modissense.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import gr.modissense.R;

public class TriToggleButton extends Button {
    // Get the attributes created in attrs.xml
    private static final int[] STATE_ONE_SET = {R.attr.state_one};
    private static final int[] STATE_TWO_SET = {R.attr.state_two};
    private static final int[] STATE_THREE_SET = {R.attr.state_three};
    private String DEBUGTAG = "CustonButtonExample";
    // Keeps track of the current state, 0, 1, or 2
    private int _state;

    // Constructors
    public TriToggleButton(Context context) {
        super(context);

        // Set the default state and text
        _state = 0;
        this.setText("No Sort");

    }

    public TriToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Set the default state and text
        _state = 0;
        this.setText("No Sort");
    }

    public TriToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Set the default state and text
        _state = 0;
        this.setText("No Sort");
    }

    @Override
    public boolean performClick() {
        // Move to the next state
        nextState();

        return super.performClick();
    }

    // Generate the drawable needed for the current state
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // Add the number of states you have
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);

        if (_state == 0) {
            mergeDrawableStates(drawableState, STATE_ONE_SET);
        } else if (_state == 1) {
            mergeDrawableStates(drawableState, STATE_TWO_SET);
        } else if (_state == 2) {
            mergeDrawableStates(drawableState, STATE_THREE_SET);
        }

        return drawableState;
    }

    // Returns current state
    public int getState() {
        return _state;
    }

    // Set current state, 0-2
    public void setState(int state) {
        if ((state > -1) && (state < 3)) {
//                      Log.d(DEBUGTAG, "   Setting Toggle state to " + state);
            _state = state;
            setButtonText();
        }
    }

    // Increases state, or loops to 0
    public void nextState() {
        _state++;

        // Loop around if at the last state
        if (_state > 2) {
            _state = 0;
        }

        setButtonText();
//              Log.d(DEBUGTAG, "   Setting Toggle state to " + _state);
    }

    // Decreases state, or loops to 2
    public void previousState() {
        _state--;

        // Loop around if at the first state
        if (_state < 0) {
            _state = 2;
        }

        setButtonText();
        Log.d(DEBUGTAG, "   Setting Toggle state to " + _state);

    }

    // Set the text displayed on the button
    private void setButtonText() {
        switch (_state) {
            case 0:
                this.setText("No Sort");
                break;
            case 1:
                this.setText("Sort By Interest");
                break;
            case 2:
                this.setText("Sort By Hotness");
                break;
            default:
                this.setText("N/A"); // Should never happen, but just in case
                break;
        }
    }

    public static class ToggleState {
        public String name;
        public String displayName;

        public ToggleState() {
        }

        public ToggleState(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }
    }
}