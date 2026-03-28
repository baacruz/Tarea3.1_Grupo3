<?php

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'database.php';
include_once 'Gabinetes.php';

$db = new DataBase();
$instant = $db->getConnection();

$pinst = new Gabinetes($instant);

$data = json_decode(file_get_contents("php://input"));


if(isset($data))
{

    $pinst->nombre = $data->nombre;
    $pinst->direccion = $data->direccion;
    $pinst->latitud = $data->latitud;
    $pinst->longitud = $data->longitud;
    $pinst->submask = $data->submask;
    $pinst->gateway = $data->gateway;

    if($pinst->createGabinete())
    {
        http_response_code(200);
        echo json_encode( 
            array( "issuccess" => true,
            "message" => "Creado con exito"));
    }
    else
    {
        http_response_code(503); // Servicio no disponible
        echo json_encode( 
            array("issuccess" => false,
            "message" => "Error al crear"));
    }
}
else
{
    http_response_code(400);
    echo json_encode(array(
        "issuccess" => false,
        "message" => "Datos incompletos o inválidos"));

}



?>