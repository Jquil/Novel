package com.wong.novel.mvp.contract;

import com.wong.novel.base.IModel;
import com.wong.novel.base.IPresenter;
import com.wong.novel.base.IView;


public interface CommonContract {

    /**
     * CommonPresenter、CommonModel 都为abstract
     * 主要是为了继承BaseModel
     * 并且规范一些共同的行为
     * */

    interface View extends IView{

    }


    interface Presenter<V extends IView> extends IPresenter<V>{

    }


    interface Model extends IModel{

    }
}
