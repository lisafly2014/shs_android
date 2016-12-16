package com.madsglobal.android.smarthw.exception;
/**
 * A SHS error occurred on the remote Shs target.
 */
public class RemoteShsException extends Exception {
    private static final long serialVersionUID = -6901728550661937942L;

    private final int mState;

    public RemoteShsException(final String message, final int state) {
        super(message);

        mState = state;
    }

    public int getErrorNumber() {
        return mState;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " (error " + mState + ")";
    }
}