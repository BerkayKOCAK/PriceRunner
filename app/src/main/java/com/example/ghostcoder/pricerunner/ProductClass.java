package com.example.ghostcoder.pricerunner;

import java.util.ArrayList;

public class ProductClass {


    private static ArrayList<String> productArray = null;
    private static ArrayList<String> urlArray = null;


    ProductClass()
    {
        productArray = new ArrayList<String>();
        urlArray  = new ArrayList<String>();
    }

    public static ArrayList<String> getProductArray() {
        return productArray;
    }

    public static void setProductArray(ArrayList<String> productArray) {
        ProductClass.productArray = productArray;
    }

    public static ArrayList<String> getUrlArray() {
        return urlArray;
    }

    public static void setUrlArray(ArrayList<String> urlArray) {
        ProductClass.urlArray = urlArray;
    }







}