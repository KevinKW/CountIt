package com.hxchd.countit;

/**
 * Created by KevinKW on 2015-12-16.
 */
public class Zt {
    public int id;
    public String name;
    public int win;
    public int loss;
    public int draw;
    public int result;
    public int delta;


    public Zt(int id, String name) {
        this.id = id;
        this.name = name;
        this.win = 0;
        this.loss = 0;
        this.draw = 0;
        this.result = 0;
        this.delta = 0;
    }

    public String toString() {
        return this.id + ":" + this.name;
    }
}
