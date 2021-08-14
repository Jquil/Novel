package com.wong.novel.mvp.contract;


public interface MainContract {

    interface View extends CommonContract.View{

    }


    interface Presenter extends CommonContract.Presenter<View>{

    }


    interface Model extends CommonContract.Model{

    }
}
