package com.grayraven.rx1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private static final String TAG = "RxMainActivity";
    private Subscription sizeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sizeSubscription =  getBitmapSizeObservable()
                      .subscribeOn(Schedulers.io()) //use the 'io' thread for this task
                      .observeOn(AndroidSchedulers.mainThread()) // pass the results to the main UI thread
                      .subscribe(new Subscriber<Integer>() {
                          @Override
                          public void onCompleted() {
                              Log.d(TAG, "Observable complete");
                              sizeSubscription.unsubscribe();
                          }

                          @Override
                          public void onError(Throwable e) {
                              Log.e(TAG, "Observable error: " + e.getMessage());
                          }

                          @Override
                          public void onNext(Integer size) {
                              Log.d(TAG, "Observable onNext");
                              Snackbar.make(fab, "Bitmap size: " + size, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                          }
                      });

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //recommended practice to avoid leaks
        if(sizeSubscription !=null && !sizeSubscription.isUnsubscribed()) {
            sizeSubscription.unsubscribe();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private Integer getBitmapSize() {
        try {
            String theUrl = "https://homepages.cae.wisc.edu/~ece533/images/airplane.png";
            java.net.URL url = new java.net.URL(theUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();As
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap.getByteCount();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Observable<Integer> getBitmapSizeObservable() {
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.just(getBitmapSize());
            }
        });
    }
}




