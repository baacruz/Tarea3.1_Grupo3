package com.example.geolocalizacion32.Configuracion;

public class ApiConfig {
    // 10.0.2.2 es la IP para que el emulador vea el localhost
    public static final String BASE_URL = "http://10.0.2.2/crud-php/";

    public static final String EndPointPost = BASE_URL + "PostGabinetes.php";
    public static final String EndPointGet = BASE_URL + "GetGabinetes.php";
    public static final String EndPointUpdate = BASE_URL + "UpdateGabinetes.php";
    public static final String EndPointDelete = BASE_URL + "DeleteGabinetes.php";
}
