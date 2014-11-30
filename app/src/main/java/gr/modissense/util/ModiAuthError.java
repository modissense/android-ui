package gr.modissense.util;

import android.util.Log;


public class ModiAuthError extends Throwable {

    private static final long serialVersionUID = 1L;
    private final Exception innerException;

    /**
     * Constructor
     *
     * @param message User readable message for the error
     * @param e       Inner exception that may be used for further debugging
     */
    public ModiAuthError(String message, Exception e) {
        super(message);
        this.innerException = e;
        Log.d("ModiAuthError", e.toString());
    }

    /**
     * Returns the inner exception
     *
     * @return Inner exception
     */
    public Exception getInnerException() {
        return innerException;
    }

}
