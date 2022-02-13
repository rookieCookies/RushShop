package com.blocky.dev;


import com.blocky.dev.rushshop.RushShop;

@SuppressWarnings("ALL")
public class Config {
    public static final Configuration i = new Configuration(RushShop.getInstance().getConfig());

    Config() { throw new IllegalStateException("Utility class"); }
}

