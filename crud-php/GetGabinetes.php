<?php

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'database.php';
include_once 'Gabinetes.php';

$db = new DataBase();
$instant = $db->getConnection();

$pinst = new Gabinetes($instant);
$cmd = $pinst->GetListGabinetes();
$count = $cmd->rowCount();

if($count > 0)
{
    $gabinetearray = array();

    while($row = $cmd->fetch(PDO::FETCH_ASSOC))
    {
        extract($row);
        $e = array(
            "id" => $id,
            "nombre" => $nombre,
            "direccion" => $direccion,
            "latitud" => $latitud,
            "longitud" => $longitud,
            "submask" => $submask,
            "gateway" => $gateway
        );


        array_push($gabinetearray, $e);
    }


    http_response_code(200);
    echo json_encode($gabinetearray);
}
else
{
    http_response_code(404);
    echo json_encode( 
        array( "issuccess" => false,
               "message" => "No hay Datos")
    );
}

?>