package com.example.pda.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private static String text;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue(text);
    }

    public static void setText(String s) {
        text = s;
    }

    public LiveData<String> getText() {
        return mText;
    }
}