package com.myscrap.service;

import android.os.Binder;

import java.lang.ref.WeakReference;

/**
 * Created by ms3 on 4/5/2017.
 */

public class LocalBinder<S> extends Binder {
    private final WeakReference<S> mService;

    LocalBinder(final S service) {
        mService = new WeakReference<>(service);
    }

    public S getService() {
        return mService.get();
    }

}
