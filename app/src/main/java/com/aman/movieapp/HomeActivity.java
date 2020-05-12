package com.aman.movieapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aman.movieapp.Adapter.MovieAdapter;
import com.aman.movieapp.Model.Movie;
import com.aman.movieapp.Model.MovieResult;
import com.aman.movieapp.Networking.MovieService;
import com.aman.movieapp.Networking.RetrofitClient;
import com.aman.movieapp.Utils.OnItemClickListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends AppCompatActivity {

    //Set standard sort mode to Popular
    private static int SORT_MODE = 1;
    private static int PAGE = 1;

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;

    private ImageView mNoInternetIv;
    private TextView mNoInternetTv;

    private List<Movie> moviesData;
    private Menu menu;

    private Call<MovieResult> movieResultCall;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mNoInternetIv = findViewById(R.id.no_internet_iv);
        mNoInternetTv = findViewById(R.id.no_internet_tv);

        recyclerView = findViewById(R.id.rv_movie);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);


        boolean flag = checkInternetConnectivity();
        if (flag){
            showRecyclerView();
        }else {
            showNoConnection();
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        item.setIcon(R.drawable.ic_sort);
        switch (item.getItemId()) {
            case R.id.action_popular:
                SORT_MODE = 1;
                setTitle(this.getString(R.string.app_name));
                break;
            case R.id.action_most_rated:
                SORT_MODE = 2;
                setTitle(this.getString(R.string.top_rated));
                break;
        }
        loadPage(PAGE);
        return super.onOptionsItemSelected(item);
    }


    private void loadPage(final int page) {
        MovieService movieService = RetrofitClient.getRetrofitClient(HomeActivity.this);
        switch (SORT_MODE) {
            case 1:
                movieResultCall = movieService.getPopularMovies(page, HomeActivity.this.getString(R.string.apiKey));
                break;

            case 2:
                movieResultCall = movieService.getTopRatedMovies(page, HomeActivity.this.getString(R.string.apiKey));
                break;
        }

        movieResultCall.enqueue(new Callback<MovieResult>() {
            @Override
            public void onResponse(Call<MovieResult> call, Response<MovieResult> response) {
                if (response.isSuccessful()){
                    if (page ==1){
                        if (response.body() != null) {
                            generateData(response.body().getMovies());
                        }
                    }else{
                        List<Movie> movieList = null;
                        if (response.body() != null) {
                            movieList = response.body().getMovies();
                        }
                        if (movieList != null) {
                            for (Movie movie: movieList){
                                moviesData.add(movie);
                                movieAdapter.notifyItemChanged(moviesData.size()-1);

                            }
                        }

                    }
                }

            }

            @Override
            public void onFailure(Call<MovieResult> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void generateData(List<Movie> movies){
        movieAdapter = new MovieAdapter(movies, new OnItemClickListener() {
            @Override
            public void onItemClick(Movie movie) {
                Log.d("Movie Clicked", movie.getTitle());

                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("movieData", movie);
                intent.putExtras(bundle);
                startActivity(intent);

                Toast.makeText(HomeActivity.this, "Item Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(movieAdapter);
    }


    public void showNoConnection(){
        mNoInternetIv.setVisibility(View.VISIBLE);
        mNoInternetTv.setVisibility(View.VISIBLE);
    }

    public void showRecyclerView(){
        loadPage(PAGE);
        mNoInternetIv.setVisibility(View.INVISIBLE);
        mNoInternetTv.setVisibility(View.INVISIBLE);
    }

    private boolean checkInternetConnectivity() {
        ConnectivityManager connectivityManager =(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();

        }
        return networkInfo != null && networkInfo.isConnected();

    }


}
