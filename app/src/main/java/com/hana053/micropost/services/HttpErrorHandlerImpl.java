package com.hana053.micropost.services;

import android.content.Context;
import android.widget.Toast;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.adapter.rxjava.HttpException;
import timber.log.Timber;

class HttpErrorHandlerImpl implements HttpErrorHandler {

    private final Context context;

    HttpErrorHandlerImpl(Context context) {
        this.context = context;
    }

    public void handleError(Throwable throwable) {
        try {
            throw throwable;
        } catch (SocketTimeoutException e) {
            Toast.makeText(context, "Connection timed out.", Toast.LENGTH_LONG).show();
        } catch (ConnectException e) {
            Toast.makeText(context, "Cannot connect to server.", Toast.LENGTH_LONG).show();
        } catch (HttpException e) {
            if (e.code() == 401) {
                Toast.makeText(context, "Please sign in.", Toast.LENGTH_LONG).show();
//                loginService.logout();
            } else if (e.code() >= 500) {
                Toast.makeText(context, "Something bad happened.", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable e) {
            Timber.e(e, "handleHttpError: %s", e.getMessage());
            Toast.makeText(context, "Something bad happened.", Toast.LENGTH_LONG).show();
        }
    }
}
