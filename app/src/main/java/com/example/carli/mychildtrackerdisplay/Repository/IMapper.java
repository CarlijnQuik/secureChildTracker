package com.example.carli.mychildtrackerdisplay.Repository;

public interface IMapper<From, To> {

    To map(From from);
}
