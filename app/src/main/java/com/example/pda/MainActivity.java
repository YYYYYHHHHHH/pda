package com.example.pda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.example.pda.bean.UserBean;
import com.example.pda.bean.globalbean.MyToast;
import com.example.pda.ui.home.HomeViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private Toast toast = MyToast.getToast();
    private NavigationView navigationView;
    private UserBean userBean;
    private SharedPreferences setinfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.dataInit();
        this.initNavigationView();
        this.initMenu();
    }
    private void dataInit() {
        setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
        String user = setinfo.getString("user", "");
        userBean = new Gson().fromJson(user, UserBean.class);
        HomeViewModel.setText("欢迎回来！" + userBean.getUser());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }
    private void initNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                if ("成品待入库".equals(item.getTitle())) {
                    intent = new Intent(MainActivity.this, ChoiceHouse.class);
                    startActivity(intent);
                } else if ("组托单".equals(item.getTitle())) {
                    intent = new Intent(MainActivity.this, GroupUserChoiceActivity.class);
                    startActivity(intent);
                }
                toast.setText("菜单名字：" + item.getTitle());
                toast.setDuration(Toast.LENGTH_LONG);
                toast.show();
                return false;
            }
        });
    }
    private void initMenu() {
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.getItem(0);
        SubMenu subMenu = item.getSubMenu();
        subMenu.add(0,10,0,"测试一下");
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}