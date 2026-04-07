package com.prueba_cuscatlan.shopping_Car_miguel.util;

public final class Constants {

    private Constants() {
    }

    public static final String API_VERSION = "/api/v1";
    public static final String PRODUCTS_PATH = API_VERSION + "/products";
    public static final String CARTS_PATH = API_VERSION + "/carts";

    // External proxy endpoints (no version public-facing, mirrors FakeStore
    // structure)
    public static final String ORDERS_PATH = API_VERSION + "/orders";
    public static final String PAYMENTS_PATH = API_VERSION + "/payments";
    public static final String EXTERNAL_PRODUCTS_PATH = "/api/products";
}
