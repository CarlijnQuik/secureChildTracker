package com.example.carli.mychildtrackerdisplay.Model;

public class DataWrapper<T> {
    public T data;
    public Throwable error;

    public DataWrapper(T data, Throwable error){
        this.data = data;
        this.error = error;
    }

    public DataWrapper(){

    }

    public T getData(){
        return this.data;
    }

    public void setData(T data){
        this.data = data;
    }

    public Throwable getError(){
        return this.error;
    }

    public void setError(Throwable error){
        this.error = error;
    }

}
