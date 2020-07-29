package com.example.pda.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("欢迎回来！销售员一");
    }

    public  void setText(String s) {
        mText.setValue(s);
    }

    public LiveData<String> getText() {
        return mText;
    }
}