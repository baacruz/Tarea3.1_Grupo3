<?php

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: DELETE");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'database.php';
include_once 'Gabinetes.php';

$db = new DataBase();
$instant = $db->getConnection();

$pinst = new Gabinetes($instant);
$data = json_decode(file_get_contents("php://input"));

if (isset($data) && !empty($data->id)) {
    $pinst->id = $data->id;

    if ($pinst->deleteGabinete()) {
        http_response_code(200);
        echo json_encode([
            "issuccess" => true,
            "message" => "Registro eliminado correctamente"
        ]);
    } else {
        http_response_code(503);
        echo json_encode([
            "issuccess" => false,
            "message" => "No se pudo eliminar el registro"
        ]);
    }
} else {
    http_response_code(400);
    echo json_encode([
        "issuccess" => false,
        "message" => "ID no proporcionado"
    ]);
}
?>