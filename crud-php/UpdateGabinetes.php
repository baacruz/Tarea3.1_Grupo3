<?php

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: PUT");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'database.php';
include_once 'Gabinetes.php';

$db = new DataBase();
$instant = $db->getConnection();

$pinst = new Gabinetes($instant);
$data = json_decode(file_get_contents("php://input"));

if (
    isset($data) &&
    !empty($data->id) &&
    !empty($data->nombre) &&
    !empty($data->direccion)&&
    !empty($data->latitud) &&
    !empty($data->longitud) &&
    !empty($data->submask)&&
    !empty($data->gateway)
) {
    $pinst->id = $data->id;
    $pinst->nombre = $data->nombre;
    $pinst->direccion = $data->direccion;
    $pinst->latitud = $data->latitud?? '';
    $pinst->longitud = $data->longitud?? '';
    $pinst->submask = $data->submask?? '';
    $pinst->gateway = $data->gateway?? '';

    if ($pinst->updateGabinete()) {
        http_response_code(200);
        echo json_encode([
            "issuccess" => true,
            "message" => "Registro actualizado correctamente"
        ]);
    } else {
        http_response_code(503);
        echo json_encode([
            "issuccess" => false,
            "message" => "No se pudo actualizar el registro"
        ]);
    }
} else {
    http_response_code(400);
    echo json_encode([
        "issuccess" => false,
        "message" => "Datos incompletos"
    ]);
}
?>